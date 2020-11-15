package com.example.gmallorder.service;

import com.example.gmallorder.vo.OrderConfirmVO;

public interface OrderService {
    /**
     * 确认订单并返回订单vo
     * @return
     */
    OrderConfirmVO confirm();

}
