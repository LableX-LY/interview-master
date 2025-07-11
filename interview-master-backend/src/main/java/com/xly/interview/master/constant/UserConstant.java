package com.xly.interview.master.constant;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 12:00
 * @description
 **/
/**
 * @author X-LY。
 * @version 1.0
 * @className UserConstant
 * @description 用户常量,用户Session中记录用户的登录态
 **/
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

}
