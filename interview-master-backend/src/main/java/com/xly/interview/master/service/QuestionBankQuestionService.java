package com.xly.interview.master.service;

import com.xly.interview.master.model.bean.QuestionBankQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xly.interview.master.model.bean.User;

import java.util.List;

/**
* @author x-ly
* @description 针对表【question_bank_question(题库题目)】的数据库操作Service
* @createDate 2025-07-03 15:02:22
*/
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    void batchAddQuestionsToBank(Long questionBankId, List<Long> questionIds, User loginUser);

    void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId);

    void batchDeleteQuestions(List<Long> questionIdList);

    void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions);

}
