package com.xly.interview.master.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xly.interview.master.annotation.AuthCheck;
import com.xly.interview.master.common.BaseResponse;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.ResultUtil;
import com.xly.interview.master.constant.UserConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.QuestionBank;
import com.xly.interview.master.model.dto.questionbank.QuestionBankAddRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankDeleteRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankEditRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankQueryRequest;
import com.xly.interview.master.model.vo.questionbank.QuestionBankVO;
import com.xly.interview.master.service.QuestionBankService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 16:14
 * @description 题库控制器
 **/
@RestController
@RequestMapping("/question/bank")
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @ApiOperation("添加题库")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest questionBankAddRequest,
                                              HttpServletRequest request) {
        if (questionBankAddRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String title = questionBankAddRequest.getTitle();
        String description = questionBankAddRequest.getDescription();
        String picture = questionBankAddRequest.getPicture();
        if (StringUtils.isAnyBlank(title, description)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long l = questionBankService.addQuestionBank(title, description, picture, request);
        return ResultUtil.success(l);
    }

    @ApiOperation("删除题库")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody QuestionBankDeleteRequest questionBankDeleteRequest) {
        if (questionBankDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        Long id = questionBankDeleteRequest.getId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目id错误!");
        }
        Boolean result = questionBankService.deleteQuestionBank(id);
        return ResultUtil.success(result);
    }

    @ApiOperation("修改题库信息")
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editQuestionBank(@RequestBody QuestionBankEditRequest questionBankEditRequest,
                                                  HttpServletRequest request) {
        if (questionBankEditRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        Long id = questionBankEditRequest.getId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目id错误!");
        }
        Boolean result = questionBankService.updateQuestionBank(questionBankEditRequest, request);
        return ResultUtil.success(result);
    }

    @ApiOperation("分页获取题库信息,管理员使用")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest) {
        if (questionBankQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        int current = questionBankQueryRequest.getCurrent();
        int pageSize = questionBankQueryRequest.getPageSize();
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, pageSize),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        return ResultUtil.success(questionBankPage);
    }

    @ApiOperation("分页获取题库信息,用户使用")
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                       HttpServletRequest request) {
        if (questionBankQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        long current = questionBankQueryRequest.getCurrent();
        long pageSize = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求数据过大!");
        }
        Page<QuestionBank> questionBankPageVO = questionBankService.page(new Page<>(current, pageSize),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        return ResultUtil.success(questionBankService.getQuestionBankVOPage(questionBankPageVO,request));
    }

}
