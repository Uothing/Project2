package com.leyou.client;

import com.leyou.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/16 10:18
 * @description:
 */
@FeignClient("user-service")
public interface UserClient {

    @GetMapping("query")
    UserDTO queryUserByUsernameAndPassword(
            @RequestParam("username") String username, @RequestParam("password") String password);
}
