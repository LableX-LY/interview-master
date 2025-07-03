package com.xly.interview.master.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.model.bean.Question;
import com.xly.interview.master.service.QuestionService;
import com.xly.interview.master.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author x-ly
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




