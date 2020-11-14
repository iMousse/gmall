package com.example.gmall.oms.dao;

import com.example.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-14 11:02:20
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
