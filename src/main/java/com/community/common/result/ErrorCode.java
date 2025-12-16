package com.community.common.result;

public enum ErrorCode {
    SUCCESS(0, "success"),
    PARAM_ERROR(40001, "参数错误"),
    UNAUTHORIZED(40101, "未登录"),
    FORBIDDEN(40301, "无权限"),
    NOT_FOUND(40401, "资源不存在"),
    BIZ_ERROR(50001, "业务错误"),
    SYSTEM_ERROR(50000, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
