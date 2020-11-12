package com.example.gmall.cart.service;

import com.example.gmall.cart.pojo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     * @param cart
     */
    void addCart(Cart cart);

    /**
     * 查询购物车
     * @return
     */
    List<Cart> getCart();

}
