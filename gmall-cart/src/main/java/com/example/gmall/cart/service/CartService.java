package com.example.gmall.cart.service;


import com.example.gmall.cart.vo.Cart;

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

    /**
     * 更新购物车
     * @param cart
     */
    void updateCart(Cart cart);

    void deleteCart(Long skuId);

    /**
     * 根据用户id查询选中的商品
     * @param userId
     * @return
     */
    List<Cart> queryCheckedByUserId(Long userId);
}
