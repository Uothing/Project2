package com.leyou.interceptors;

import com.leyou.common.auth.JwtUtils;
import com.leyou.common.auth.Payload;
import com.leyou.common.auth.UserInfo;
import com.leyou.common.threadlocal.UserHolder;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/19 10:46
 * @description:
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private static final String COOKIE_NAME = "LY_TOKEN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 从cookie中获取
            String token = CookieUtils.getCookieValue(request, COOKIE_NAME);
            // 解析token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            UserInfo userInfo = payload.getUserInfo();
            // 保存user
            UserHolder.setUser(userInfo);
            return true;
        } catch (UnsupportedEncodingException e) {
            // 解析失败，不继续向下
            log.error("【购物车服务】解析用户信息失败！", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
