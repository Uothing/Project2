package com.leyou.gateway.filter;

import com.leyou.common.auth.JwtUtils;
import com.leyou.common.auth.Payload;
import com.leyou.common.auth.UserInfo;
import com.leyou.common.auth.UserTokenConstants;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/16 21:30
 * @description:
 */
@Slf4j
@Component
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProp;
    /**
     * 过滤器类型
     * @return pre:前置、Post：后置、route:路由、error：异常
     */
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * 当过滤器类型一样时，用order判断优先级，值越小，优先级越高
     */
    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER + 1;
    }

    /**
     * 过滤器是否生效
     * @return true：生效，则run方法执行。false：不生效，过滤器不会对任何请求处理
     */
    @Override
    public boolean shouldFilter() {
        // 1.获取请求对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 2.判断是否符合规范
        boolean isAllow  = isAllowRequest(request);
        // 如果符合白名单规范，要放行，返回false；不符合规范，返回true
        return !isAllow;
    }

    private boolean isAllowRequest(HttpServletRequest request) {
        // 获取正在请求的路径
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        for (Map.Entry<String, String> entry : filterProp.getAllowPathAndMethod().entrySet()) {
            // 获取允许的请求路径
            String path = entry.getKey();
            // 获取允许的请求方式
            String method = entry.getValue();

            // 判断是否符合
            if(requestURI.startsWith(path) && ("*".equals(method) || requestMethod.equalsIgnoreCase(method))){
                return true;
            }
        }
        return false;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 过滤器拦截的逻辑
     */
    @Override
    public Object run() throws ZuulException {
        //	1.获取请求对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {

            //	2.获取请求中的cookie
            String token = CookieUtils.getCookieValue(request, UserTokenConstants.COOKIE_NAME);
            //	3.解析cookie中的token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            //	4.验证黑名单
            String id = payload.getId();
            Boolean exists = redisTemplate.hasKey(id);
            if(BooleanUtils.isTrue(exists)){
                // 黑名单中存在，无效token
                throw new RuntimeException("无效的token！");
            }
            //	5.登录信息有效，放行

            //	TODO 6.获取用户身份、查询用户权限、判断是否可以访问当前资源
            UserInfo userInfo = payload.getUserInfo();
            String username = userInfo.getUsername();
            String role = userInfo.getRole();
            String path = request.getRequestURI();

            log.info("用户{}，角色{}，正在访问{}资源。", username, role, path);
        } catch (RuntimeException e) {
            // 拦截用户请求
            ctx.setSendZuulResponse(false);
            // 没有访问权限，被禁止
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
