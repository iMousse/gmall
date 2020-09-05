package com.example.gmall.pms.vo;

import com.example.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.example.gmall.pms.entity.AttrEntity;
import com.example.gmall.pms.entity.AttrGroupEntity;

import java.util.List;

public class GroupVO extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relations;

    public List<AttrEntity> getAttrEntities() {
        return attrEntities;
    }

    public void setAttrEntities(List<AttrEntity> attrEntities) {
        this.attrEntities = attrEntities;
    }

    public List<AttrAttrgroupRelationEntity> getRelations() {
        return relations;
    }

    public void setRelations(List<AttrAttrgroupRelationEntity> relations) {
        this.relations = relations;
    }
}
