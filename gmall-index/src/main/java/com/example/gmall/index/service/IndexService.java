package com.example.gmall.index.service;

import com.example.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {

    /**
     * 查询1级目录
     * @return
     */
    List<CategoryEntity> queryCate1();

}
