package com.xly.interview.master.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.service.UserService;
import com.xly.interview.master.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author x-ly
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




