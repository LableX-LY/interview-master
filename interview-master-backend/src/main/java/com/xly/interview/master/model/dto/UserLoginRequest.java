package com.xly.interview.master.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 09:47
 * @description
 **/
@Data
public class UserLoginRequest implements Serializable {

    private String userAccount;

    private String userPassword;

    private static final long serialVersionUID = 3191241716373120793L;

}
