package com.example.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.ums.entity.GrowthChangeHistoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 成长值变化历史记录
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-10 16:45:18
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageVo queryPage(QueryCondition params);
}

