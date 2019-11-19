package com.leyou.web;

import com.leyou.common.auth.UserInfo;
import com.leyou.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/16 10:26
 * @description:
 */
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录授权
     * @param username 用户名
     * @param password 密码
     * @param response 响应
     * @return 无
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response) {

        authService.login(username, password, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证用户、同时保证用户浏览期间cookie的有效性（少于10分钟，重新发）
     * @param request
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(HttpServletRequest request, HttpServletResponse response) {


        return ResponseEntity.ok(authService.verifyUser(request, response));
    }

    /**
     * 退出
     * @param request
     * @param response
     * @return
     */
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        authService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
