package com.example.gmall.search.controller;

import com.atguigu.core.bean.Resp;
import com.example.gmall.search.service.SearchService;
import com.example.gmall.search.vo.SearchParamVO;
import com.example.gmall.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Resp<SearchResponseVO> search(SearchParamVO searchParamVO) throws IOException {
        return Resp.ok( this.searchService.search(searchParamVO));
    }

}
