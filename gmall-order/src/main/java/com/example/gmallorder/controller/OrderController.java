package com.example.gmallorder.controller;


import com.atguigu.core.bean.Resp;
import com.example.gmallorder.service.OrderService;
import com.example.gmallorder.vo.OrderConfirmVO;
import com.example.gmall.oms.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {

        OrderConfirmVO confirmVO = this.orderService.confirm();

        return Resp.ok(confirmVO);
    }

    @PostMapping("submit")
    public Resp submit(@RequestBody OrderSubmitVO submitVO) {
        this.orderService.submit(submitVO);
        return Resp.ok("提交成功");
    }
}
