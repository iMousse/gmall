package com.example.gmall.order.service;

import com.example.gmall.order.vo.OrderConfirmVO;
import com.example.gmall.oms.vo.OrderSubmitVO;

public interface OrderService {
    /**
     * 确认订单并返回订单vo
     * @return
     */
    OrderConfirmVO confirm();

    /**
     * 订单提交
     * @param submitVO
     */
    void submit(OrderSubmitVO submitVO);
}
