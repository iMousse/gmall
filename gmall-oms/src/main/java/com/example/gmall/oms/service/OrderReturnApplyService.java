package com.example.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.oms.entity.OrderReturnApplyEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 订单退货申请
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-14 11:02:20
 */
public interface OrderReturnApplyService extends IService<OrderReturnApplyEntity> {

    PageVo queryPage(QueryCondition params);
}

