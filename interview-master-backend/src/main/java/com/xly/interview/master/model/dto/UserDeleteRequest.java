package com.xly.interview.master.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 15:50
 * @description 删除用户请求类
 **/
@Data
public class UserDeleteRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;

}
