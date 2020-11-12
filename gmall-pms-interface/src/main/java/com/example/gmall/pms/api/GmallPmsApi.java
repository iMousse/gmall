package com.example.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.entity.*;
import com.example.gmall.pms.vo.CategoryVO;
import com.example.gmall.pms.vo.ItemGroupVO;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("pms/skuinfo/info/{skuId}")
    Resp<SkuInfoEntity> querySkuInfoBySkuId(@PathVariable("skuId") Long skuId);


    @GetMapping("pms/skuimages/{skuId}")
    Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);


    @GetMapping("pms/skusaleattrvalue/{spuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValueBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/spuinfodesc/info/{spuId}")
    Resp<SpuInfoDescEntity> querySpuDescBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandInfo(@PathVariable("brandId") Long brandId);


    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryInfo(@PathVariable("catId") Long catId);


    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 查询分类
     *
     * @param level
     * @param pid
     * @return
     */
    @GetMapping("pms/category")
    Resp<List<CategoryEntity>> queryByCategoriesByPidOrLevel(@RequestParam(value = "level", defaultValue = "0") Integer level,
                                                             @RequestParam(value = "parentCid", required = false) Long pid);


    @GetMapping("pms/category/{pid}")
    Resp<List<CategoryVO>> querySubCategories(@PathVariable("pid") Long pid);

    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    Resp<List<ItemGroupVO>> queryItemGroupVOByCidAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);


    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValueBySkuId(@PathVariable("skuId") Long skuId);


}
