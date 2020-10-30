package com.example.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.pms.vo.CategoryVO;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryVO> querySubCategories(Long pid);
}

