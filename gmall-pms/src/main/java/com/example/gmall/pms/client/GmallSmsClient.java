package com.example.gmall.pms.client;

import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.vo.SkuSaleVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("sms-service")
public interface GmallSmsClient {


    @PostMapping("/sms/skubounds/sku/sale/save")
    Resp<Object> saveSales(@RequestBody SkuSaleVO skuSaleVO);


}
