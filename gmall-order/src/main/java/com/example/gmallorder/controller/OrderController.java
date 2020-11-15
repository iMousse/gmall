package com.example.gmallorder.controller;


import com.atguigu.core.bean.Resp;
import com.example.gmallorder.service.OrderService;
import com.example.gmallorder.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {

        OrderConfirmVO confirmVO =  this.orderService.confirm();

        return Resp.ok(confirmVO);
    }

}
