package com.example.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gmall.oms.client.GmallPmsClient;
import com.example.gmall.oms.client.GmallUmsClient;
import com.example.gmall.oms.dao.OrderDao;
import com.example.gmall.oms.dao.OrderItemDao;
import com.example.gmall.oms.entity.OrderEntity;
import com.example.gmall.oms.entity.OrderItemEntity;
import com.example.gmall.oms.service.OrderService;
import com.example.gmall.oms.vo.OrderItemVO;
import com.example.gmall.oms.vo.OrderSubmitVO;
import com.example.gmall.pms.entity.SkuInfoEntity;
import com.example.gmall.pms.entity.SpuInfoEntity;
import com.example.gmall.ums.entity.MemberEntity;
import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public OrderEntity saveOrder(OrderSubmitVO orderSubmitVO, Long userId) {
        //保存orderEntity
        OrderEntity orderEntity = new OrderEntity();

        MemberReceiveAddressEntity address = orderSubmitVO.getAddress();
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());

        MemberEntity member = this.umsClient.queryMemberByUserId(userId).getData();
        orderEntity.setMemberId(userId);
        orderEntity.setMemberUsername(member.getUsername());

        //清算每个商品赠送的积分
        orderEntity.setIntegration(0);
        orderEntity.setGrowth(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setStatus(0);

        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setSourceType(1);
        orderEntity.setPayType(orderSubmitVO.getPayType());
        String orderToken = orderSubmitVO.getOrderToken();
        orderEntity.setOrderSn(orderToken);

        this.save(orderEntity);
        Long orderId = orderEntity.getId();


        //保存orderItemEntity
        List<OrderItemVO> orderItems = orderSubmitVO.getOrderItems();
        orderItems.forEach(orderItem -> {
            OrderItemEntity itemEntity = new OrderItemEntity();

            // 订单信息
            itemEntity.setOrderId(orderId);
            itemEntity.setOrderSn(orderToken);

            // 需要远程查询spu信息 TODO
            SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(orderItem.getSkuId()).getData();
            SpuInfoEntity spuInfo = this.pmsClient.querySpuById(skuInfo.getSpuId()).getData();

            // 设置sku信息
            itemEntity.setSkuPrice(skuInfo.getPrice());
            itemEntity.setSkuAttrsVals(JSON.toJSONString(orderItem.getSaleAttrValue()));
            itemEntity.setCategoryId(skuInfo.getCatalogId());
            itemEntity.setSpuId(spuInfo.getId());
            itemEntity.setSpuName(spuInfo.getSpuName());
            itemEntity.setSkuName(skuInfo.getSkuName());
            itemEntity.setSkuPic(skuInfo.getSkuDefaultImg());
            itemEntity.setSkuQuantity(orderItem.getCount());

            this.orderItemDao.insert(itemEntity);
        });

        //订单创建之后，在响应之前发送延时队列，达到定时关单的效果
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "order.ttl", orderToken);

        return orderEntity;
    }

}