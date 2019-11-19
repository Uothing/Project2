package com.leyou.config;

import com.leyou.common.auth.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private String priKeyPath;
    /**
     * 用户token相关属性
     */
    private UserTokenProperties user = new UserTokenProperties();

    private PublicKey publicKey;
    private PrivateKey privateKey;

    // 在Spring完成属性初始化后再加载密钥
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败!", e);
            throw new RuntimeException(e);
        }
    }

    @Data
    public class UserTokenProperties {
        /**
         * token过期时长
         */
        private int expire;
        /**
         * 存放token的cookie名称
         */
        private String cookieName;
        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;
    }
}