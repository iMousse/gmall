package com.example.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.pms.vo.GroupVO;


/**
 * 属性分组
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 根据分页条件和分类id查询分页参数
     * @param condition  分页条件
     * @param catId  分类id
     * @return
     */
    PageVo getGroupByCidAndPage(QueryCondition condition, Long catId);

    /**
     * 通过分组id查询attrs
     * @param gid 分组id
     * @return
     */
    GroupVO queryGroupWIthAttrsByGid(Long gid);
}

