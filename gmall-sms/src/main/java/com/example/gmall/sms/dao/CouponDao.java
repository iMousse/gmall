package com.example.gmall.sms.dao;

import com.example.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 19:41:56
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
