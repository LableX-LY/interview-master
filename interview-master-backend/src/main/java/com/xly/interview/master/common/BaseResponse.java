package com.xly.interview.master.common;

import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 16:25
 * @description 通用的响应类
 **/

public class BaseResponse<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

    public BaseResponse(int code, T data, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
