package com.leyou.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.utils.RegexUtils;
import com.leyou.config.SmsProperties;
import com.leyou.utils.SmsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/13 20:12
 * @description:
 */
@Slf4j
@Component
public class SmsListener {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private SmsHelper smsHelper;

    //验证验证码
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE, durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.SMS_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.VERIFY_CODE_KEY
    ))
    public void verifyCode(Map<String, String> msg) {

        if (CollectionUtils.isEmpty(msg)) {
            //如果为空，放弃
            return;
        }

        //获取手机号
        String phone = msg.get("phone");
        if (!RegexUtils.isPhone(phone)) {
            log.error("手机号格式不正确:{}", phone);
            return;
        }

        //获取验证码 {"code": "123123"}

        String code = msg.get("code");
        if (!RegexUtils.isCode(code)) {
            log.error("手机号格式不正确:{}", code);
            return;
        }

        String param = "{\"code\": \""+code+"\"}";

        smsHelper.sendMessage(phone, smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate(), param);
    }
}
