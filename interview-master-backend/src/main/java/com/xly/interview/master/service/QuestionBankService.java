package com.xly.interview.master.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xly.interview.master.model.bean.QuestionBank;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xly.interview.master.model.dto.questionbank.QuestionBankAddRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankEditRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankQueryRequest;
import com.xly.interview.master.model.vo.questionbank.QuestionBankVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author x-ly
* @description 针对表【question_bank(题库)】的数据库操作Service
* @createDate 2025-07-03 15:02:22
*/
public interface QuestionBankService extends IService<QuestionBank> {

    Long addQuestionBank(String title, String description, String picture, HttpServletRequest request);

    Boolean deleteQuestionBank(Long id);

    Boolean updateQuestionBank(QuestionBankEditRequest questionBankEditRequest, HttpServletRequest request);

    QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest);

    Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage, HttpServletRequest request);

}
