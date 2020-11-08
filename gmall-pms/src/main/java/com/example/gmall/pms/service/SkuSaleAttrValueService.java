package com.example.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * sku销售属性&值
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 根据spuId查询sku销售属性
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValueEntity> querySkuSaleAttrValueBySpuId(Long spuId);
}

