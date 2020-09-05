package com.example.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.pms.vo.AttrVO;


/**
 * 商品属性
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     *
     * 根据分类id和属性类型查询Attr
     * @param queryCondition
     * @param cid
     * @param type
     * @return
     */
    PageVo queryByCidAndType(QueryCondition queryCondition, Long cid, Integer type);

    /**
     * 维护属性组和属性的中间表
     *
     * @param attr
     */
    void saveAttr(AttrVO attr);

    /**
     * 维护属性组和属性的中间表
     *
     * @param attr
     */
    void updateAttrById(AttrVO attr);
}

