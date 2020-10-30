package com.example.gmall.pms.vo;

import com.example.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {

    private List<CategoryEntity> subs;
}
