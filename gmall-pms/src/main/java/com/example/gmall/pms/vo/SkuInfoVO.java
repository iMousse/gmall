package com.example.gmall.pms.vo;

import com.example.gmall.pms.entity.SkuInfoEntity;
import com.example.gmall.pms.entity.SkuSaleAttrValueEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVO extends SkuInfoEntity {


    //sku销售属性值
    private List<SkuSaleAttrValueEntity> saleAttrs;

    //sku图片
    private List<String> images;

    //积分营销的相关字段
    @ApiModelProperty(name = "growBounds", value = "成长积分")
    private BigDecimal growBounds;
    @ApiModelProperty(name = "buyBounds", value = "购物积分")
    private BigDecimal buyBounds;
    @ApiModelProperty(name = "work", value = "优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]")
    private List<Integer> work;


    //打折相关字段
    @ApiModelProperty(name = "fullCount", value = "满几件")
    private Integer fullCount;
    @ApiModelProperty(name = "discount", value = "打几折")
    private BigDecimal discount;
    @ApiModelProperty(name = "addOther", value = "是否叠加其他优惠[0-不可叠加，1-可叠加]")
    private Integer ladderAddOther;

    //满减相关字段
    @ApiModelProperty(name = "fullPrice",value = "满多少")
    private BigDecimal fullPrice;
    @ApiModelProperty(name = "reducePrice",value = "减多少")
    private BigDecimal reducePrice;
    @ApiModelProperty(name = "addOther",value = "是否参与其他优惠")
    private Integer fullAddOther;



}
