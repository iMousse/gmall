package com.example.gmall.ums.dao;

import com.example.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-11-10 16:45:18
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
