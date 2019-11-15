package com.leyou.mq;

import com.leyou.common.constants.MQConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsListenerTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void verifyCode() throws InterruptedException {

        String code = RandomStringUtils.randomNumeric(6);
        System.out.println("code = " + code);

        Map<String,String> map = new HashMap<>();
        map.put("phone", "13600523456");
        map.put("code", code);
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME, MQConstants.RoutingKey.VERIFY_CODE_KEY, map);

    }

}