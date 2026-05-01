package com.aquaculture.exception;

import com.aquaculture.common.ResultCode;
import lombok.Getter;

/**
 * Alert runtime exception for triggering alerts
 */
@Getter
public class AlertException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public AlertException(String message) {
        super(message);
        this.code = ResultCode.ALERT_TRIGGERED.getCode();
    }

    public AlertException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AlertException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public AlertException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

}
