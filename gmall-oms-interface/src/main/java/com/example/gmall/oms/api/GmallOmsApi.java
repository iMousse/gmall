package com.example.gmall.oms.api;

import com.atguigu.core.bean.Resp;
import com.example.gmall.oms.entity.OrderEntity;
import com.example.gmall.oms.vo.OrderSubmitVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    @PostMapping("oms/order/{userId}")
    Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVO orderSubmitVO, @PathVariable("userId") Long userId);
}
