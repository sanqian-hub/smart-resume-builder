package com.smart.resume.common;

import lombok.Getter;

/**
 * 错误码（枚举值）
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "目标请求数据为空", ""),
    NO_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "没有权限", ""),
    NO_FOUND_ERROR(40102, "未找到", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    FILE_UPLOAD_ERROR(50001, "文件上传失败", "");

    /**
     * code: 返回状态码
     * message： 状态码信息（简单）
     * description：状态码描述（详情）
     */
    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
