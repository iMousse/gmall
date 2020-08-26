package com.example.gmall.pms.dao;

import com.example.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
