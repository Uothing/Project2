package com.leyou.web;

import com.leyou.entity.User;
import com.leyou.service.UserService;
import com.leyou.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/13 21:22
 * @description:
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //校验 数据库中用户表 数据是否可用（重复）
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable(value = "type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    //发生验证码
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //注册
    @PostMapping("register")
    public ResponseEntity<Void> register(User user, @RequestParam("code") String code){
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //根据用户名密码查找
    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username") String username, @RequestParam("password") String password){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));
    }
}
