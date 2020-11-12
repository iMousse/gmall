package com.example.gmall.auth.service.impl;

import com.atguigu.core.utils.JwtUtils;
import com.example.gmall.auth.client.GmallUmsClient;
import com.example.gmall.auth.config.JwtProperties;
import com.example.gmall.auth.service.AuthService;
import com.example.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
//@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public String authentication(String username, String password) {
        MemberEntity member = this.umsClient.queryUser(username, password).getData();
        if (member == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", member.getId());
        map.put("username", member.getUsername());
        try {
            return JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
