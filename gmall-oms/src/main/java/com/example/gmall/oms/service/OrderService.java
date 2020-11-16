package com.example.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.oms.entity.OrderEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.oms.vo.OrderSubmitVO;


/**
 * 订单
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-14 11:02:20
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity saveOrder(OrderSubmitVO orderSubmitVO,Long userId);
}

