package com.example.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.sms.entity.SpuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品spu积分设置
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 19:41:56
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageVo queryPage(QueryCondition params);
}

