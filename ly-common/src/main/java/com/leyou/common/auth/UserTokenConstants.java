package com.leyou.common.auth;

public class UserTokenConstants {
    /**
     * 用户token的cookie名称
     */
    public static final String COOKIE_NAME = "LY_TOKEN";
    /**
     * 用户token的cookie的domain
     */
    public static final String DOMAIN = "leyou.com";
    /**
     * 用户token的过期时间
     */
    public static final int EXPIRE_MINUTES = 30;

    /**
     * 刷新时间
     */
    public static final int MIN_REFRESH_INTERVAL = 20;
}