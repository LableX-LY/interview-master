package com.xly.interview.master.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/16 16:00
 * @description
 **/
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}