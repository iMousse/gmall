package com.example.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.wms.entity.WareInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 仓库信息
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageVo queryPage(QueryCondition params);
}

