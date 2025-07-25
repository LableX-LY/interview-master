package com.xly.interview.master.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/19 09:22
 * @description 题目添加请求类
 **/
@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 推荐答案
     */
    private String answer;

    private static final long serialVersionUID = 1L;

}
