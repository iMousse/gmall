package com.example.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallPmsApi {

    /**
     * 分页查询spu
     *
     * @param condition
     * @return
     */
    @PostMapping("pms/spuinfo/page")
    Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition condition);


    @GetMapping("pms/spuinfo/info/{id}")
    Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);


    @GetMapping("pms/skuinfo/{spuId}")
    Resp<List<SkuInfoEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandInfo(@PathVariable("brandId") Long brandId);


    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryInfo(@PathVariable("catId") Long catId);


    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);



}
