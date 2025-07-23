package com.xly.interview.master.controller;

import com.xly.interview.master.annotation.AuthCheck;
import com.xly.interview.master.common.BaseResponse;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.ResultUtil;
import com.xly.interview.master.constant.UserConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.model.dto.UserDeleteRequest;
import com.xly.interview.master.model.dto.UserLoginRequest;
import com.xly.interview.master.model.dto.UserRegisterRequest;
import com.xly.interview.master.model.vo.user.LoginUserVO;
import com.xly.interview.master.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 15:20
 * @description
 **/
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkedPassword = userRegisterRequest.getCheckedPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkedPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        long resutl = userService.userRegister(userAccount, userPassword, checkedPassword);
        if (resutl < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"新增用户失败");
        }
        return ResultUtil.success(resutl);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtil.success(loginUserVO);
    }

    @ApiOperation("删除用户信息，管理员可用")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest) {
        if (userDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userDeleteRequest.getId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户ID错误");
        }
        Boolean result = userService.deleteUser(id);
        return ResultUtil.success(result);
    }

    @ApiOperation("用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户未登录!");
        }
        //清空Session
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return ResultUtil.success(true);
    }

    @ApiOperation("用户签到")
    @PostMapping("/add/sign_in")
    public BaseResponse<Boolean> userSignIn(HttpServletRequest request) {
        User user = userService.getloginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录!");
        }
        return ResultUtil.success(userService.addUserSignIn(user.getId()));
    }

    @ApiOperation("查看用户签到记录")
    @GetMapping("/get/sign_in")
    public BaseResponse<List<Integer>> getUserSignIn(Integer year, HttpServletRequest request) {
        User user = userService.getloginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"请先登录!");
        }
        // Map<LocalDate, Boolean> userSignInRecord = userService.getUserSignInRecord(user.getId(), year);
        List<Integer> userSignInRecord = userService.getUserSignInRecord(user.getId(), year);
        return ResultUtil.success(userSignInRecord);
    }

}
