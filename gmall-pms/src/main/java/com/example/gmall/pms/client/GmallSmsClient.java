package com.example.gmall.pms.client;

import com.example.gmall.sms.client.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
