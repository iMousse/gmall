package com.example.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.sms.entity.SpuFullReductionEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品满减信息
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 19:41:56
 */
public interface SpuFullReductionService extends IService<SpuFullReductionEntity> {

    PageVo queryPage(QueryCondition params);
}

