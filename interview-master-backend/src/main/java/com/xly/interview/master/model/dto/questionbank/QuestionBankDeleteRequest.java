package com.xly.interview.master.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/16 14:55
 * @description 题目删除请求类
 **/
@Data
public class QuestionBankDeleteRequest implements Serializable {

    /**
     * 题库id
     */
    private Long id;

    private static final long serialVersionUID = 1L;

}
