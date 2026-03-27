package com.smart.resume.common;

import com.smart.resume.exception.BusinessException;
import lombok.Data;

import java.io.Serializable;


/**
 * 统一返回类
 * @param <T> 泛型用来指代返回数据的类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    // 全参构造
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    // data为null的构造
    public BaseResponse(int code, String message, String description) {
        this.code = code;
        this.data = null;
        this.message = message;
        this.description = description;
    }

    // 缺少description的构造，description用null
    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    // message和description都用null
    public BaseResponse(int code, T data) {
        this(code, data, null, "");
    }

    // 这里处理失败的时候，我们data返回null
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null,
                errorCode.getMessage(), errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode, T data) {
        this(errorCode.getCode(), data,
                errorCode.getMessage(), errorCode.getDescription());
    }


    // 成功
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    // 失败
    public static <T> BaseResponse<T> fail(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    // 失败
    public static <T> BaseResponse<T> fail(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), description);
    }

    // 失败
    public static <T> BaseResponse<T> fail(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), message, description);
    }

    // 失败
    public static <T> BaseResponse<T> fail(int code, String message, String description) {
        return new BaseResponse<>(code, message, description);
    }

    // 失败：参数接收一个BusinessException的重载，封装，容易扩展
    public static <T> BaseResponse<T> fail(BusinessException businessException) {
        return BaseResponse.fail(businessException.getCode(),
                businessException.getMessage(), businessException.getDescription());
    }
}
