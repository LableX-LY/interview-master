package com.xly.interview.master.common;

import com.xly.interview.master.constant.CommonConstant;
import lombok.Data;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 16:28
 * @description 通用的分页查询请求类
 **/
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;

}
