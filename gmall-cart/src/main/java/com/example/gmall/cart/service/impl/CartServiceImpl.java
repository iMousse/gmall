package com.example.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.gmall.cart.client.GmallPmsClient;
import com.example.gmall.cart.client.GmallSmsClient;
import com.example.gmall.cart.client.GmallWmsClient;
import com.example.gmall.cart.interceptor.LoginInterceptor;
import com.example.gmall.cart.pojo.Cart;
import com.example.gmall.cart.pojo.UserInfo;
import com.example.gmall.cart.service.CartService;
import com.example.gmall.pms.entity.SkuInfoEntity;
import com.example.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX_KEY = "gmall:cart:";
    private static final String PRICE_PREFIX_KEY = "gmall:sku:price";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public void addCart(Cart cart) {
        String key = CART_PREFIX_KEY;
        //根据userId和userKey判断登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getId() != null) {
            key += userInfo.getId();
        } else {
            key += userInfo.getUserKey();
        }

        //获取用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();
        //判断购物车是否有该记录
        if (hashOps.hasKey(skuId)) {
            //有新增数量
            String cartJson = hashOps.get(skuId).toString();//获取购物车的sku信息
            cart = JSON.parseObject(cartJson, Cart.class);//转换成Cart对象
            cart.setCount(cart.getCount() + count);//更新购物车数量

        } else {
            //没有则新增商品
            cart.setCheck(true);
            SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(cart.getSkuId()).getData();
            cart.setTitle(skuInfo.getSkuTitle());
            cart.setDefaultImage(skuInfo.getSkuDefaultImg());
            cart.setPrice(skuInfo.getPrice());
            cart.setSales(this.smsClient.querySkuSalesBySkuId(cart.getSkuId()).getData());
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId()).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
            //将当前价格放入redis中
            this.redisTemplate.opsForValue().set(PRICE_PREFIX_KEY + skuId, skuInfo.getPrice().toString());
        }


        hashOps.put(skuId, JSON.toJSONString(cart));//重新写入redis


    }

    @Override
    public List<Cart> getCart() {
        //获取登陆状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询未登录的购物车，如果未登录则返回购物车
        List<Cart> unloginCartList = null;
        String unloginKey = CART_PREFIX_KEY + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unloginHashOps = this.redisTemplate.boundHashOps(unloginKey);
        List<Object> cartJsonList = unloginHashOps.values();

        if (!CollectionUtils.isEmpty(cartJsonList)) {
            unloginCartList = cartJsonList.stream().map(cartJson ->{
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX_KEY + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }

        if (userInfo.getId() == null) {
            return unloginCartList;
        }

        //如果登录，对不是空的未登录的购物车进行同步，并删除未登录的购物车
        String loginKey = CART_PREFIX_KEY + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unloginCartList)) {
            unloginCartList.forEach(cart -> {
                Integer count = cart.getCount();
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
            this.redisTemplate.delete(unloginKey);
        }

        List<Object> loginCartJSONList = loginHashOps.values();
        return loginCartJSONList.stream().map(cartJSON -> {
            Cart cart = JSON.parseObject(cartJSON.toString(), Cart.class);
            //查询当前价格
            String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX_KEY + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(currentPrice));
            return cart;
        }).collect(Collectors.toList());


    }

    @Override
    public void updateCart(Cart cart) {
        //获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        String key = CART_PREFIX_KEY;
        //根据登陆信息来更新redis的购物车
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }

        //获取用户购物车并根据cart的skuId修改
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();
        if (hashOps.hasKey(skuId)) {
            String cartJson = hashOps.get(skuId).toString();
            Integer count = cart.getCount();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(skuId, JSON.toJSONString(cart));
        }
    }

    @Override
    public void deleteCart(Long skuId) {
        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 获取redis的key
        String key = CART_PREFIX_KEY;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        hashOperations.delete(skuId.toString());
    }
}
