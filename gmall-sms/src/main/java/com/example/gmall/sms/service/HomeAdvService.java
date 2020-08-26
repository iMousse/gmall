package com.example.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.sms.entity.HomeAdvEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 首页轮播广告
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 19:41:56
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageVo queryPage(QueryCondition params);
}

