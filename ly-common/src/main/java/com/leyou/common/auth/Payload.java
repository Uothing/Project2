package com.leyou.common.auth;

import lombok.Data;

import java.util.Date;

@Data
public class Payload<T> {
    /**
     * token的唯一标示
     */
    private String id;
    /**
     * 过期时间
     */
    private Date expiration;
    /**
     * 签发时间
     */
    private Date issueAt;
    /**
     * 用户信息
     */
    private T userInfo;
}