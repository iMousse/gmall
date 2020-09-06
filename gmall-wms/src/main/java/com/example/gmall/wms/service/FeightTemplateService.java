package com.example.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.wms.entity.FeightTemplateEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 运费模板
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
public interface FeightTemplateService extends IService<FeightTemplateEntity> {

    PageVo queryPage(QueryCondition params);
}

