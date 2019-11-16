package com.leyou.service;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.entity.User;
import com.leyou.mapper.UserMapper;
import com.leyou.user.dto.UserDTO;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Boolean checkData(String data, Integer type) {

        User user = new User();
        if (type == 1) {
            user.setUsername(data);
        } else if (type == 2) {
            user.setPhone(data);
        } else {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        int count = userMapper.selectCount(user);
        return count == 0;
    }


    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private final static String KEY_PREFIX = "user:send:code:phone:";
    //发送验证码
    public void sendCode(String phone) {
        //验证手机号格式
        if (!RegexUtils.isPhone(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }



        // 生成验证码
        String code = RandomStringUtils.randomNumeric(6);

        // 保存验证码到redis
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 2, TimeUnit.MINUTES);

        // 发送RabbitMQ消息到ly-sms
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME, MQConstants.RoutingKey.VERIFY_CODE_KEY, msg);
    }


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 注册
    public void register(User user, String code) {
        // 校验验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        // 比较验证码
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        // 对密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 写入数据库
        int count = userMapper.insertSelective(user);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    //根据用户名密码查找
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        // 1根据用户名查询
        User u = new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        // 2判断是否存在
        if (user == null) {
            // 用户名错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        // 3校验密码
        if(!passwordEncoder.matches(password, user.getPassword())){
            // 密码错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return BeanHelper.copyProperties(user, UserDTO.class);
    }
}