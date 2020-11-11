package com.example.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.ums.entity.IntegrationChangeHistoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 积分变化历史记录
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-10 16:45:18
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageVo queryPage(QueryCondition params);
}

