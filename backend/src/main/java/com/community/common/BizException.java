package com.community.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final ResultCode code;

    public BizException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }
}
