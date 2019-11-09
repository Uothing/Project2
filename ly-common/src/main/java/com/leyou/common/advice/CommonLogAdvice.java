package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/2 20:57
 * @description: 完善的日志记录
 */
@Slf4j
@Aspect
@Component
public class CommonLogAdvice {

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object handleExceptionLog(ProceedingJoinPoint joinPoint) {
        log.debug("{}方法准备调用，参数：{}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
        try {
            long begin = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            log.debug("{}方法调用成功，耗时{}ms", joinPoint.getSignature(), end - begin);
            return result;
        } catch (Throwable throwable) {
            log.error("{}方法调用异常，原因：{}", joinPoint.getSignature(), throwable.getMessage(), throwable);
            if (throwable instanceof LyException) {
                throw (LyException)throwable;
            }
            throw new LyException(500, throwable);
        }
    }
}
