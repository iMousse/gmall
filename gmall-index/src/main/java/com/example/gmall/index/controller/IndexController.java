package com.example.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.example.gmall.index.service.IndexService;
import com.example.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryCate1() {

        List<CategoryEntity> categoryEntities = this.indexService.queryCate1();

        return Resp.ok(categoryEntities);
    }
}
