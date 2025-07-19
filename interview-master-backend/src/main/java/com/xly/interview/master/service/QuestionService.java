package com.xly.interview.master.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xly.interview.master.model.bean.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xly.interview.master.model.dto.question.QuestionAddRequest;
import com.xly.interview.master.model.dto.question.QuestionQueryRequest;
import com.xly.interview.master.model.vo.question.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author x-ly
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-07-03 15:02:22
*/
public interface QuestionService extends IService<Question> {

    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);

    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    Long addQuestion(QuestionAddRequest questionAddRequest, HttpServletRequest request);

    void validQuestion(Question question, boolean add);

    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

}
