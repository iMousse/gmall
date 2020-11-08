package com.example.gmall.item.vo;

import com.example.gmall.pms.entity.*;
import com.example.gmall.pms.vo.ItemGroupVO;
import com.example.gmall.sms.vo.SaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVO {
    //当前sku的基本信息
    private Long skuId;
    private CategoryEntity categoryEntity;
    private BrandEntity brandEntity;

    private Long spuId;
    private String spuName;
    private String skuTitle;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal weight;

    //sku的所有图片
    private List<SkuImagesEntity> pics;

    //sku的所有促销信息
    private List<SaleVO> sales;

    //是否有货
    private Boolean store;

    //sku的所有销售属性组合
    private List<SkuSaleAttrValueEntity> saleAttrs;

    //spu的所有基本属性
    private List<ItemGroupVO> groups;

    //详情介绍
    private List<String> desc;


}
