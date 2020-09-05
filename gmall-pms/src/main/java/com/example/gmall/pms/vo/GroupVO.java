package com.example.gmall.pms.vo;

import com.example.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.example.gmall.pms.entity.AttrEntity;
import com.example.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVO extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relations;

}
