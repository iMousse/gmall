package com.example.gmallorder.client;

import com.example.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
