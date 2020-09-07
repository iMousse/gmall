package com.example.gmall.sms.client;


import com.atguigu.core.bean.Resp;
import com.example.gmall.sms.vo.SkuSaleVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public interface GmallSmsApi {

    @PostMapping("/sms/skubounds/sku/sale/save")
    Resp<Object> saveSales(@RequestBody SkuSaleVO skuSaleVO);

}