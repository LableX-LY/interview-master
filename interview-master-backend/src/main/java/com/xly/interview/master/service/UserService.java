package com.xly.interview.master.service;

import com.xly.interview.master.model.bean.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xly.interview.master.model.vo.user.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author x-ly
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-03 15:02:22
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkedPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getloginUser(HttpServletRequest request);

    Boolean deleteUser(Long id);

}
