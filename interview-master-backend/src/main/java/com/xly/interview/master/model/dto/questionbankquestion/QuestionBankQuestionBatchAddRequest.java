package com.xly.interview.master.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/23 15:45
 * @description
 **/
@Data
public class QuestionBankQuestionBatchAddRequest implements Serializable {

    /**
     * 题库ID
     */
    private Long questionBankId;

    /**
     * 题目ID列表
     */
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;

}
