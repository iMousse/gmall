package com.example.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员收货地址
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-10 16:45:18
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageVo queryPage(QueryCondition params);
}

