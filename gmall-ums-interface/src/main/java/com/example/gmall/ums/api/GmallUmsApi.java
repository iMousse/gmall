package com.example.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.example.gmall.ums.entity.MemberEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface GmallUmsApi {

    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
}
