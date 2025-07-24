package com.xly.interview.master.controller;

import com.xly.interview.master.annotation.AuthCheck;
import com.xly.interview.master.common.BaseResponse;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.ResultUtil;
import com.xly.interview.master.constant.UserConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.QuestionBankQuestion;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.model.dto.questionbankquestion.QuestionBankQuestionBatchAddRequest;
import com.xly.interview.master.model.dto.questionbankquestion.QuestionBankQuestionBatchRemoveRequest;
import com.xly.interview.master.model.dto.questionbankquestion.QuestionBatchDeleteRequest;
import com.xly.interview.master.service.QuestionBankQuestionService;
import com.xly.interview.master.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/23 15:49
 * @description 题库-题目关联控制器
 **/
@RestController
@RequestMapping("/questionBank/question")
public class QuestionBankQuesitonController {

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    public QuestionBankQuesitonController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("批量向题库中添加题目")
    @PostMapping("/add/batch")
    public BaseResponse<Boolean> batchAddQuestionsToBank(@RequestBody QuestionBankQuestionBatchAddRequest questionBankQuestionBatchAddRequest,
                                                              HttpServletRequest request) {
        if (questionBankQuestionBatchAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        Long questionBankId = questionBankQuestionBatchAddRequest.getQuestionBankId();
        List<Long> questionIdList = questionBankQuestionBatchAddRequest.getQuestionIdList();
        User loginUser = userService.getloginUser(request);
        questionBankQuestionService.batchAddQuestionsToBank(questionBankId, questionIdList, loginUser);
        return ResultUtil.success(true);
    }

    @ApiOperation("批量从题库中移除题目")
    @PostMapping("/remove/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchRemoveQuestionsFromBank(
            @RequestBody QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        if(questionBankQuestionBatchRemoveRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long questionBankId = questionBankQuestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionIdList = questionBankQuestionBatchRemoveRequest.getQuestionIdList();
        questionBankQuestionService.batchRemoveQuestionsFromBank(questionIdList, questionBankId);
        return ResultUtil.success(true);
    }

    @ApiOperation("批量删除指定的题目")
    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteQuestions(@RequestBody QuestionBatchDeleteRequest questionBatchDeleteRequest,
                                                      HttpServletRequest request) {
        if (questionBatchDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        questionBankQuestionService.batchDeleteQuestions(questionBatchDeleteRequest.getQuestionIdList());
        return ResultUtil.success(true);
    }


}
