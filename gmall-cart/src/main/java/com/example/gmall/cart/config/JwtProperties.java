package com.example.gmall.cart.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "gmall.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private PublicKey publicKey;
    private String cookieName;
    private String userKeyName;
    private Integer expire;

    @PostConstruct
    public void init() {
        try {
            RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取公钥路径错误");
        }
    }
}
