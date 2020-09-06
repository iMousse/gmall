package com.example.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.wms.entity.ShAreaEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 全国省市区信息
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
public interface ShAreaService extends IService<ShAreaEntity> {

    PageVo queryPage(QueryCondition params);
}

