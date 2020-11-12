package com.example.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.example.gmall.cart.pojo.Cart;
import com.example.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart) {

        this.cartService.addCart(cart);

        return Resp.ok("添加成功");
    }

    @GetMapping
    public Resp<List<Cart>> queryCarts() {
        List<Cart> carts = this.cartService.getCart();
        return Resp.ok(carts);
    }
}
