package com.xly.interview.master.model.vo;

import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 09:48
 * @description 已登录用户的脱敏视图
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static LoginUserVO objToVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

}
