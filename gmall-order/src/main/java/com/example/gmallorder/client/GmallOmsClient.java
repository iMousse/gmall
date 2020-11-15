package com.example.gmallorder.client;

import com.example.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
