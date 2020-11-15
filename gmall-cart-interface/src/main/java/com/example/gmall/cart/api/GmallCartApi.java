package com.example.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.example.gmall.cart.vo.Cart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallCartApi {

    @GetMapping("cart/{userId}")
    Resp<List<Cart>> queryCartByUserId(@PathVariable("userId") Long userId);

}
