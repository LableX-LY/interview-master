package com.xly.interview.master.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 09:47
 * @description 用户注册请求类
 **/
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 二次确认密码
     */
    private String checkedPassword;

    private static final long serialVersionUID = 3191241716373120793L;

}
