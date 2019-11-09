package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/30 20:47
 * @description:
 */
@ControllerAdvice
@Slf4j
public class BasicExceptionAdvice {

    //抓取500错误
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        log.error(e.getMessage(),e);
        return ResponseEntity.status(500).body(e.getMessage());
    }

    //自定义错误
    @ExceptionHandler(LyException.class)
    public ResponseEntity<String> lyException(LyException e) {
        //打印控制台错误信息
        log.error(e.getMessage(),e);
        //响应到浏览器
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    /*//抓取404错误  浏览器地址输错？？？？？？？？？
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException1(RuntimeException e) {
        log.error(e.getMessage(),e);
        return ResponseEntity.status(404).body(e.getMessage());
    }*/
}
