package com.example.gmall.wms.api;

import com.atguigu.core.bean.Resp;
import com.example.gmall.wms.entity.WareSkuEntity;
import com.example.gmall.wms.vo.SkuLockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {

    @GetMapping("wms/waresku/{skuId}")
    Resp<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);


    @PostMapping("wms/waresku")
    Resp checkAndLockStore(@RequestBody List<SkuLockVO> skuLockVOS);

}
