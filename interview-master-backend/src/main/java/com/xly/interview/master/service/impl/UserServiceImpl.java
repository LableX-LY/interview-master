package com.xly.interview.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.model.vo.user.LoginUserVO;
import com.xly.interview.master.service.UserService;
import com.xly.interview.master.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xly.interview.master.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author x-ly
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "InterviewMaster";

    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkedPassword 二次确认密码
     * @return 用户ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkedPassword) {
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkedPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        }
        // 1.参数校验
        this.checkAccountAndPassword(userAccount,userPassword,checkedPassword);
        // 2.查询数据库是否有相同账号
        synchronized (userAccount.intern()) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("userAccount",userAccount);
            Long l = this.baseMapper.selectCount(userQueryWrapper);
            if(l>0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
            }
        }
        // 3.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 4.存入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        int insert = userMapper.insert(user);
        if(insert < 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"新增用户失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @return 登录用户视图
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空!");
        }
        if (userAccount.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度需大于8位");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度需大于8位");
        }
        // 1.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 2.查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        if(user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在或密码错误");
        }
        // 3.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        // 4.返回LoginUserVO
        return LoginUserVO.objToVO(user);
    }

    @Override
    public Boolean deleteUser(Long id) {
        if (id < 0 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户ID为空");
        }
        // 1.先查询该用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"要删除的用户不存在!");
        }
        int i = userMapper.deleteById(id);
        if (i < 0) {
            return false;
        }
        return true;
    }

    /**
     * 校验账号和密码的合法性
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkedPassword 二次确认密码
     */
    private void checkAccountAndPassword(String userAccount, String userPassword, String checkedPassword) {
        // 长度判断
        if(userAccount.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度需大于8位!");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需大于8位!");
        }
        if (checkedPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需大于8位!");
        }
        // 内容判断
        String validPatternUserAccount = "[`~!#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~!#¥%,,,,,,&*（）——+｜{}【】'; ;]";
        Matcher userAccountMatcher = Pattern.compile(validPatternUserAccount).matcher(userAccount);
        if(userAccountMatcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含禁止使用的特殊字符!");
        }
        String validPatternUserPassword = "[`~!#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~!#¥%,,,,,,&*（）——+｜{}【】'; ;]";
        Matcher userPasswordMatcher = Pattern.compile(validPatternUserPassword).matcher(userPassword);
        if(userPasswordMatcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含禁止使用的特殊字符!");
        }
        // 二次确认密码判断
        if (!userPassword.equals(checkedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致!");
        }
    }

    @Override
    public User getloginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否已经登录
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询（追求性能的话可以注释，直接返回上述结果）
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}




