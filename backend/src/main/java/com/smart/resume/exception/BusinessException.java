package com.smart.resume.exception;

import com.smart.resume.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    // 继承运行时异常，我们在java代码里可以不用显式地去捕获他
    // 这样就不用到处去写try-catch或者throws的样版代码，到处都是污染

    private final String description;

    private final int code;

    public BusinessException(String message, String description, int code) {
        super(message);
        this.description = description;
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getMessage(), errorCode.getDescription(), errorCode.getCode());
    }

    public BusinessException(ErrorCode errorCode, String description) {
        this(errorCode.getMessage(), description, errorCode.getCode());
    }

}
