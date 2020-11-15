package com.example.gmallorder.service.impl;

import com.atguigu.core.bean.UserInfo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.gmall.cart.vo.Cart;
import com.example.gmall.pms.entity.SkuInfoEntity;
import com.example.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.example.gmall.ums.entity.MemberEntity;
import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import com.example.gmall.wms.entity.WareSkuEntity;
import com.example.gmallorder.client.*;
import com.example.gmallorder.interceptor.LoginInterceptor;
import com.example.gmallorder.service.OrderService;
import com.example.gmallorder.vo.OrderConfirmVO;
import com.example.gmallorder.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

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
            String idStr = IdWorker.getIdStr();
            orderConfirm.setOrderToken(idStr);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressCompletableFuture, skuCompletableFuture, tokenCompletableFuture,memberCompletableFuture).join();

        return orderConfirm;
    }
}
