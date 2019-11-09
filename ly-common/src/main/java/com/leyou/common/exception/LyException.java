package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/30 21:20
 * @description:
 */
@Getter
public class LyException extends RuntimeException {
    /**
     * 异常状态码
     */
    private int status;
    public LyException(ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }
    public LyException(ExceptionEnum em, Throwable cause) {
        super(em.getMessage(), cause);
        this.status = em.getStatus();
    }
    public LyException(int status, String message) {
        super(message);
        this.status = status;
    }
    public LyException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;

    }

    public LyException(int status, Throwable cause) {
        super(cause);
        this.status = status;

    }
}
