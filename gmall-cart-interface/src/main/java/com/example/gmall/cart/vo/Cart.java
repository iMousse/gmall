package com.example.gmall.cart.vo;

import com.example.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.example.gmall.sms.vo.SaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private Long skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private BigDecimal currentPrice;
    private Integer count;
    private Boolean store;
    private List<SkuSaleAttrValueEntity> saleAttrValue;
    private List<SaleVO> sales;
    private Boolean check;
}
