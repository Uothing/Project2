package com.leyou.service;

import com.leyou.client.UserClient;
import com.leyou.common.auth.JwtUtils;
import com.leyou.common.auth.Payload;
import com.leyou.common.auth.UserInfo;
import com.leyou.common.auth.UserTokenConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.user.dto.UserDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/16 10:34
 * @description:
 */
@Service
public class AuthService {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private UserClient userClient;

    private static final String USER_ROLE = "role_user";

    public void login(String username,  String password, HttpServletResponse response) {

        try {
            //查询用户
            UserDTO user = userClient.queryUserByUsernameAndPassword(username, password);

            //转成userInfo
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), USER_ROLE);

            // 生成token
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
            // 写入cookie
            CookieUtils.newCookieBuilder()
                    .response(response) // response,用于写cookie
                    .httpOnly(true) // 保证安全防止XSS攻击，不允许JS操作cookie
                    .domain(prop.getUser().getCookieDomain()) // 设置domain
                    .name(prop.getUser().getCookieName()).value(token) // 设置cookie名称和值
                    .build();// 写cookie
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    // 验证用户、同时保证用户浏览期间cookie的有效性（少于10分钟，重新发）
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 获取cookie
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            // 解析cookie
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);

            // 判断是否在黑名单中
            String id = payload.getId();
            Boolean hasKey = redisTemplate.hasKey(id);
            if (BooleanUtils.isTrue(hasKey)) {
                // 登录失效
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            // cookie的有效期
            Date expiration = payload.getExpiration();
            // 获取刷新时间
            DateTime refreshTime = new DateTime(expiration.getTime()).minusMinutes(UserTokenConstants.MIN_REFRESH_INTERVAL);
            // 判断是否已经过了刷新时间
            if (refreshTime.isBefore(System.currentTimeMillis())) {
                // 如果过了刷新时间，则生成新token
                token = JwtUtils.generateTokenExpireInMinutes(payload.getUserInfo(), prop.getPrivateKey(), UserTokenConstants.EXPIRE_MINUTES);
                // 写入cookie
                CookieUtils.newCookieBuilder()
                        // response,用于写cookie
                        .response(response)
                        // 保证安全防止XSS攻击，不允许JS操作cookie
                        .httpOnly(true)
                        // 设置domain
                        .domain(UserTokenConstants.DOMAIN)
                        // 设置cookie名称和值
                        .name(UserTokenConstants.COOKIE_NAME).value(token)
                        // 写cookie
                        .build();
            }
            // 返回
            return payload.getUserInfo();
        } catch (Exception e) {
            // 登录失效
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 退出
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取cookie
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
        if (StringUtils.isBlank(token)) {
            // 如果为空，直接结束
            return;
        }
        // 解析cookie
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        String id = payload.getId();
        // 剩余时间
        long remainTime = payload.getExpiration().getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(id, "1", remainTime, TimeUnit.MILLISECONDS);

        //删除cookie
        CookieUtils.deleteCookie(UserTokenConstants.COOKIE_NAME, UserTokenConstants.DOMAIN,response);
    }
}
