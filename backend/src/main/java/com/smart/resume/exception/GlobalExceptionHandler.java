package com.smart.resume.exception;

import com.smart.resume.common.BaseResponse;
import com.smart.resume.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("businessException: {} ", e.getMessage());
        return BaseResponse.fail(e);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException : {} ",e.getMessage(), e);
        return BaseResponse.fail(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("参数错误");
        return BaseResponse.fail(ErrorCode.PARAMS_ERROR, msg);
    }

}
