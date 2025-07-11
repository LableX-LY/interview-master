package com.xly.interview.master.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 15:43
 * @description
 **/
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    VIP("会员", "vip"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }

}
