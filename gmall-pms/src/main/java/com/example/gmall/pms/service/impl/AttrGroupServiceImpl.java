package com.example.gmall.pms.service.impl;

import com.example.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.example.gmall.pms.dao.AttrDao;
import com.example.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.example.gmall.pms.entity.AttrEntity;
import com.example.gmall.pms.service.AttrAttrgroupRelationService;
import com.example.gmall.pms.vo.GroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.example.gmall.pms.dao.AttrGroupDao;
import com.example.gmall.pms.entity.AttrGroupEntity;
import com.example.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrDao attrDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo getGroupByCidAndPage(QueryCondition condition, Long catId) {

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();

        if (catId != null) {
            wrapper.eq("catelog_id", catId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public GroupVO queryGroupWIthAttrsByGid(Long gid) {
        GroupVO groupVO = new GroupVO();

        //查询group
        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtils.copyProperties(groupEntity, groupVO);

        //查询关联关系，获取attrids
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));

        if (!CollectionUtils.isEmpty(relationEntities)) {
            groupVO.setRelations(relationEntities);
        }

        //根据attrids查询attr
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrEntities);

        //查询attrs
        return groupVO;
    }

}