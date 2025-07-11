package com.xly.interview.master.common;


/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 16:51
 * @description 全局的返回工具类
 **/

public class ResultUtil {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }

}
