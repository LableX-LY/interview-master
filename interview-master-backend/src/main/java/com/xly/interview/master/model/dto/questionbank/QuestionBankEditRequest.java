package com.xly.interview.master.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/16 15:03
 * @description 题目修改请求类
 **/
@Data
public class QuestionBankEditRequest implements Serializable {

    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String picture;

    private static final long serialVersionUID = 1L;

}
