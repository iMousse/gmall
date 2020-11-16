package com.example.gmall.oms.client;

import com.example.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {


}
