package com.leyou.gateway.config;

import com.leyou.common.auth.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    /**
     * 公钥地址
     */
    private String pubKeyPath;
    /**
     * 公钥对象
     */
    private PublicKey publicKey;

    /**
     * 用户token相关属性
     */
    private UserTokenProperties user = new UserTokenProperties();

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            log.info("【网关】加载公钥成功！");
        } catch (Exception e) {
            log.error("【网关】加载公钥失败！", e);
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