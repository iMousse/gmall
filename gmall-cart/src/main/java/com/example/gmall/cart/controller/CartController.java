package com.example.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.example.gmall.cart.service.CartService;
import com.example.gmall.cart.vo.Cart;
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

    @GetMapping("{userId}")
    public Resp<List<Cart>> queryCartByUserId(@PathVariable("userId") Long userId) {
        return Resp.ok(this.cartService.queryCheckedByUserId(userId));
    }

    @GetMapping
    public Resp<List<Cart>> queryCarts() {
        List<Cart> carts = this.cartService.getCart();
        return Resp.ok(carts);
    }

    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart) {
        this.cartService.updateCart(cart);
        return Resp.ok("更新成功");
    }

    @PostMapping("{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId")Long skuId){

        this.cartService.deleteCart(skuId);

        return Resp.ok(null);
    }
}
