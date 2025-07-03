package com.xly.interview.master.controller;

import com.xly.interview.master.model.bean.User;
import org.springframework.web.bind.annotation.*;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 15:20
 * @description
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("register")
    public User register(@RequestBody User user) {
        return user;
    }

}
