package com.example.gmallorder.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.gmall.cart.vo.Cart;
import com.example.gmall.oms.entity.OrderEntity;
import com.example.gmall.oms.vo.OrderItemVO;
import com.example.gmall.oms.vo.OrderSubmitVO;
import com.example.gmall.pms.entity.SkuInfoEntity;
import com.example.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.example.gmall.ums.entity.MemberEntity;
import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import com.example.gmall.wms.entity.WareSkuEntity;
import com.example.gmall.wms.vo.SkuLockVO;
import com.example.gmallorder.client.*;
import com.example.gmallorder.interceptor.LoginInterceptor;
import com.example.gmallorder.service.OrderService;
import com.example.gmallorder.vo.OrderConfirmVO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_TOKEN_PREFIX = "order:token:";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallOmsClient omsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public OrderConfirmVO confirm() {
        OrderConfirmVO orderConfirm = new OrderConfirmVO();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        if (userId == null) {
            return null;
        }


        //收集异步请求的集合
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        //获取用户的id查询收货地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            List<MemberReceiveAddressEntity> addressEntities = this.umsClient.queryAddressByUserId(userId).getData();
            orderConfirm.setAddresses(addressEntities);
        }, threadPoolExecutor);
        futures.add(addressCompletableFuture);

        //获取购物车中选中的商品信息
        //线程池异步请求返回参数后设置
        CompletableFuture<Void> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //获取购物车中选中的商品信息
            List<Cart> carts = this.cartClient.queryCartByUserId(userId).getData();
            if (CollectionUtils.isEmpty(carts)) {
                throw new RuntimeException("请勾选购物车商品");
            }
            return carts;
        }, threadPoolExecutor).thenAcceptAsync(carts -> {
            orderConfirm.setOrderItems(carts.stream().map(cart -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                Long skuId = cart.getSkuId();

                //线程池异步请求，不返回参数
                CompletableFuture<Void> skuInfoCompletableFuture = CompletableFuture.runAsync(() -> {
                    SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(skuId).getData();
                    if (skuInfo != null) {
                        orderItemVO.setSkuId(skuId);
                        orderItemVO.setWeight(skuInfo.getWeight());
                        orderItemVO.setCount(cart.getCount());
                        orderItemVO.setDefaultImage(skuInfo.getSkuDefaultImg());
                        orderItemVO.setTitle(skuInfo.getSkuTitle());
                        orderItemVO.setPrice(skuInfo.getPrice());
                    }
                }, threadPoolExecutor);

                //线程池异步请求，不返回参数
                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<SkuSaleAttrValueEntity> attrValueEntities = pmsClient.querySkuSaleAttrValueBySkuId(skuId).getData();
                    orderItemVO.setSaleAttrValue(attrValueEntities);
                }, threadPoolExecutor);

                //线程池异步请求，不返回参数
                CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareSkusBySkuId(skuId).getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuInfoCompletableFuture, saleAttrCompletableFuture, wareCompletableFuture).join();
                return orderItemVO;
            }).collect(Collectors.toList()));
        });


        //查询用户信息，获取积分
        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            MemberEntity memberEntity = this.umsClient.queryMemberByUserId(userId).getData();
            orderConfirm.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        //生成一个唯一标志，防止重复提交,(页面一份，Redis一份)
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getIdStr();
            orderConfirm.setOrderToken(orderToken);
            redisTemplate.opsForValue().set(ORDER_TOKEN_PREFIX + orderToken, orderToken);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressCompletableFuture, skuCompletableFuture, tokenCompletableFuture, memberCompletableFuture).join();

        return orderConfirm;
    }

    @Override
    public void submit(OrderSubmitVO submitVO) {

        //1.防止重复提交，如果第一次则放行并删除redis的token，如果不是第一次则返回
        String orderToken = submitVO.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //必须原子性，这里使用的是lua脚本
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(ORDER_TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0) {
            throw new RuntimeException("订单不可重复提交");
        }

        //2.校验商品总价格
        List<OrderItemVO> items = submitVO.getOrderItems();
        BigDecimal totalPrice = submitVO.getTotalPrice();
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("没有勾选需要购买的商品");
        }

        BigDecimal currentTotalPrice = items.stream().map(item -> {
            SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(item.getSkuId()).getData();
            if (skuInfo != null) {
                return skuInfo.getPrice().multiply(new BigDecimal(item.getCount()));
            }

            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();

        if (currentTotalPrice.compareTo(totalPrice) != 0) {
            throw new RuntimeException("页面已过期，请刷新页面后重新下单");
        }


        //3.校验库存并锁定，一次性提示所有库存不够的商品信息
        List<SkuLockVO> lockVOS = items.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(item.getSkuId());
            skuLockVO.setCount(item.getCount());
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> wareResp = this.wmsClient.checkAndLockStore(lockVOS);

        if (wareResp.getCode() != 0) {
            throw new RuntimeException(wareResp.getMsg());
        }


        //4.生成订单信息
        Long id = LoginInterceptor.getUserInfo().getId();

        OrderEntity data = this.omsClient.saveOrder(submitVO, id).getData();
        if (data == null) {
            throw new RuntimeException("服务器错误，创建订单失败");
        }

        //5.通过消息队列删除购物车，1-4是强一致性，而最后一步不需要强制执行，如果通过Feign远程调用则强一致性
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        map.put("skuIds", items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList()));
        this.amqpTemplate.convertAndSend("cart.delete", map);

    }
}
