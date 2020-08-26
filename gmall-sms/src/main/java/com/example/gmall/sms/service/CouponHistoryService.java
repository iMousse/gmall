package com.example.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.sms.entity.CouponHistoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 优惠券领取历史记录
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 19:41:56
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageVo queryPage(QueryCondition params);
}

