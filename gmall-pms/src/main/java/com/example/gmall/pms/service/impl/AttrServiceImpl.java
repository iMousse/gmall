package com.example.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.example.gmall.pms.dao.AttrDao;
import com.example.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.example.gmall.pms.entity.AttrEntity;
import com.example.gmall.pms.service.AttrService;
import com.example.gmall.pms.vo.AttrVO;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidAndType(QueryCondition queryCondition, Long cid, Integer type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        if (cid != null) {
            wrapper.eq("catelog_id", cid);
        }

        wrapper.eq("attr_type", type);


        return new PageVo(this.page(new Query<AttrEntity>().getPage(queryCondition), wrapper));
    }

    @Override
    public void saveAttr(AttrVO attr) {
        //新增attr
        this.save(attr);
        Long attrId = attr.getAttrId();

        //新增relation
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attr.getAttrGroupId());
        relationEntity.setAttrId(attrId);

        relationDao.insert(relationEntity);
    }

    @Override
    public void updateAttrById(AttrVO attr) {

        this.updateById(attr);


    }

}