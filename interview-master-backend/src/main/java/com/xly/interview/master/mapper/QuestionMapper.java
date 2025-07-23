package com.xly.interview.master.mapper;

import com.xly.interview.master.model.bean.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author x-ly
 * @description 针对表【question(题目)】的数据库操作Mapper
 * @createDate 2025-07-03 15:02:22
 * @Entity com.xly.interview.master.model.bean.Question
 */
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询题目列表（包括已被删除的数据）
     */
    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);

}




