package com.example.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;

import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "E:\\ideProject\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\ideProject\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    //    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "1234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzAxMjEyODZ9.GioCiqMt_ZcN6_RAuDBcOzcHQ5WdqdhA9QYu-2IqCQqnAef1VyXczEInj1Ef1xo7AvcjxnkIMuZK48OoczUy1iqtPQPDchUzTl03b8h_J3xMBaxOAaKSwMpm20DH25VrTgBExUafyxHwxfOa-PVHW0Kk41KrWDncayzXbZ_lYLoa9Cuvacr8eAFz-ckriIiZ9bRzFkhX-wYHSHFlym2IJRjBRhFtpkN5GLAVsmsdm-yD4eiJXqioWspqXiBSdROsjrTRiFe511yujR0y2ngL9OnZ1QH6bHDQ2WmhPTrswKjjy-HWIxk1FQ7uXtSpPa5diymmPVTWA0clys7R1MK9oQ";
//        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDUwOTQ5ODF9.c_6tKHoVyx30wgexSWia-3_oCjBHG-4haRwLSMib7TeJqin1dVcKRhtNAbYc-WfJuQlystK09pmQPKms-YBiHc7f9gml5WBQ-O9wdsJ8iL-Ruzxd0BKBAI9hOghtRMu_Ae7x3nn5ypkylQ9Aw3_e5UYC8E-36iEMNzyDblc_SkiHRIhxPJaKqmn683caPe92Y7HIcsAn_kUhmMQMOMWts2Uo7XrJbIOfSZ-GUJ-xFjdHxj5C0rKCSIyzqHPAIJSTZscuWjf1jV9Eu4QyNWFYpLQdkNiL206zSskdd6DK-VR5EwHXeZC5aq1wVi1mo3Pp7EsRum9K6AvBUERaJ46M0Q";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}