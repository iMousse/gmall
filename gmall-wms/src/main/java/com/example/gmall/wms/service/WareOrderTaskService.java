package com.example.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.wms.entity.WareOrderTaskEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 库存工作单
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageVo queryPage(QueryCondition params);
}

