package com.example.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.example.gmall.ums.entity.MemberEntity;
import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);

    @GetMapping("ums/member/info/{id}")
    Resp<MemberEntity> queryMemberByUserId(@PathVariable("id") Long id);

    @GetMapping("ums/memberreceiveaddress/{userId}")
    Resp<List<MemberReceiveAddressEntity>> queryAddressByUserId(@PathVariable("userId") Long userId);


}
