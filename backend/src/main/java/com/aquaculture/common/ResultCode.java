package com.aquaculture.common;

import lombok.Getter;

/**
 * Result code enumeration
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误"),
    BUSINESS_ERROR(1001, "业务异常"),
    ALERT_TRIGGERED(1002, "告警触发");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
