package com.example.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.wms.vo.SkuLockVO;

import java.util.List;


/**
 * 商品库存
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    String checkAndLockStore(List<SkuLockVO> skuLockVOS);
}

