package com.community.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getDefaultMessage(), data, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> error(ResultCode code, String message) {
        return new Result<>(code.getCode(), message, null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> error(ResultCode code) {
        return new Result<>(code.getCode(), code.getDefaultMessage(), null, Instant.now().toEpochMilli());
    }
}
