package com.xly.interview.master.exception;

import com.xly.interview.master.common.ErrorCode;
import lombok.Data;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 16:35
 * @description 自定义错误类型
 **/
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }

}
