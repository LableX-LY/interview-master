package com.xly.interview.master.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xly.interview.master.annotation.AuthCheck;
import com.xly.interview.master.common.BaseResponse;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.ResultUtil;
import com.xly.interview.master.constant.UserConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.Question;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.model.dto.question.QuestionAddRequest;
import com.xly.interview.master.model.dto.question.QuestionDeleteRequest;
import com.xly.interview.master.model.dto.question.QuestionQueryRequest;
import com.xly.interview.master.model.dto.question.QuestionUpdateRequest;
import com.xly.interview.master.model.vo.question.QuestionVO;
import com.xly.interview.master.service.QuestionService;
import com.xly.interview.master.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/19 08:39
 * @description 题目控制器
 **/
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @ApiOperation("分页查询某个题库内的题目,仅管理员可用")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {

        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        return ResultUtil.success(questionPage);
    }

    @ApiOperation("用户分页获取某个题库内的题目,脱敏")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空!");
        }
        long pageSize = questionQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误!");
        }
        // 查询数据库
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        // 获取封装类
        return ResultUtil.success(questionService.getQuestionVOPage(questionPage, request));
    }

    @ApiOperation("根据ID获取题目详情")
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误!");
        }
//        // 检测和处置爬虫（可以自行扩展为 - 登录后才能获取到答案）
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            crawlerDetect(loginUser.getId());
//        }
        // 友情提示，对于敏感的内容，可以再打印一些日志，记录用户访问的内容
        // 查询数据库
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在!");
        }
        // 获取封装类
        return ResultUtil.success(questionService.getQuestionVO(question, request));
    }

    @ApiOperation("管理员添加题目")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        Long l = questionService.addQuestion(questionAddRequest, request);
        return ResultUtil.success(l);
    }

    @ApiOperation("管理员删除题目")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody QuestionDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getloginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        if (oldQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在!");
        }
//        // 仅本人或管理员可删除
//        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"内部错误，题目删除失败!");
        }
        return ResultUtil.success(true);
    }

    @ApiOperation("管理员修改题目")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        if (oldQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在!");
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"内部错误，题目删除失败!");
        }
        return ResultUtil.success(true);
    }

    @PostMapping("/search/es/page/vo")
    public BaseResponse<Page<QuestionVO>> searchQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Question> questionPage = questionService.searchFromEs(questionQueryRequest);
        return ResultUtil.success(questionService.getQuestionVOPage(questionPage, request));
    }


}
