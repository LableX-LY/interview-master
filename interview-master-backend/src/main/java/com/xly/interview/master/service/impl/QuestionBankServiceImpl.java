package com.xly.interview.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.SqlUtils;
import com.xly.interview.master.constant.CommonConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.QuestionBank;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.model.dto.questionbank.QuestionBankAddRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankEditRequest;
import com.xly.interview.master.model.dto.questionbank.QuestionBankQueryRequest;
import com.xly.interview.master.model.vo.questionbank.QuestionBankVO;
import com.xly.interview.master.service.QuestionBankService;
import com.xly.interview.master.mapper.QuestionBankMapper;
import com.xly.interview.master.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author x-ly
* @description 针对表【question_bank(题库)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
    implements QuestionBankService{

    @Resource
    private QuestionBankMapper questionBankMapper;

    @Resource
    private UserService userService;

    @Override
    public Long addQuestionBank(String title, String description, String picture, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(title, description) || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        // 参数校验
        if (title.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度须小于20！");
        }
        if (description.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库描述内容过长！");
        }
        QuestionBank questionBank = new QuestionBank();
        questionBank.setTitle(title);
        questionBank.setDescription(description);
        questionBank.setPicture(picture);
        User user = userService.getloginUser(request);
        questionBank.setUserId(user.getId());
        int insert = questionBankMapper.insert(questionBank);
        if (insert < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题库添加失败!");
        }
        return questionBank.getId();
    }

    @Override
    public Boolean deleteQuestionBank(Long id) {
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目id错误!");
        }
        // 先查看该id对应的题目是否存在
        QuestionBank questionBank = questionBankMapper.selectById(id);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在，无法删除!");
        }
        int i = questionBankMapper.deleteById(questionBank);
        return i > 0;
    }


    @Override
    public Boolean updateQuestionBank(QuestionBankEditRequest questionBankEditRequest, HttpServletRequest request) {
        // 校验信息
        String title = questionBankEditRequest.getTitle();
        String description = questionBankEditRequest.getDescription();
        if (title.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度须小于20！");
        }
        if (description.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库描述内容过长！");
        }
        // 查询该题目是否存在
        Long id = questionBankEditRequest.getId();
        QuestionBank oldQuestionBank = questionBankMapper.selectById(id);
        if (oldQuestionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题库不存在，无法修改!");
        }
        QuestionBank newQuestionBank = new QuestionBank();
        BeanUtil.copyProperties(questionBankEditRequest, newQuestionBank);
        int i = questionBankMapper.updateById(newQuestionBank);
        if (i < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题库更新失败!");
        }
        return true;
    }

    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> questionBankQueryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return questionBankQueryWrapper;
        }
        // 取出要查询的字段信息
        Long id = questionBankQueryRequest.getId();
        //Long notId = questionBankQueryRequest.getNotId();
        String title = questionBankQueryRequest.getTitle();
        String searchText = questionBankQueryRequest.getSearchText();
        String sortField = questionBankQueryRequest.getSortField();
        String sortOrder = questionBankQueryRequest.getSortOrder();
        Long userId = questionBankQueryRequest.getUserId();
        String description = questionBankQueryRequest.getDescription();
        String picture = questionBankQueryRequest.getPicture();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            questionBankQueryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        // 模糊查询
        questionBankQueryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        questionBankQueryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 精确查询
        //questionBankQueryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        questionBankQueryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        questionBankQueryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        questionBankQueryWrapper.eq(ObjectUtils.isNotEmpty(picture), "picture", picture);
        // 排序规则
        questionBankQueryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return questionBankQueryWrapper;
    }

    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage, HttpServletRequest request) {
        List<QuestionBank> questionBankList = questionBankPage.getRecords();
        Page<QuestionBankVO> questionBankVOPage = new Page<>(questionBankPage.getCurrent(),
                questionBankPage.getSize(), questionBankPage.getTotal());
        if (CollUtil.isEmpty(questionBankList)) {
            return questionBankVOPage;
        }
        // 对象列表 -> 封装对象列表
        List<QuestionBankVO> questionBankVOList = questionBankList.stream().map(QuestionBankVO::objToVo).collect(Collectors.toList());

        // 关联查询创建人信息，可选
//        Set<Long> userIdSet = questionBankList.stream().map(QuestionBank::getUserId).collect(Collectors.toSet());
//        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
//                .collect(Collectors.groupingBy(User::getId));
//        // 填充信息
//        questionBankVOList.forEach(questionBankVO -> {
//            Long userId = questionBankVO.getUserId();
//            User user = null;
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            questionBankVO.setUser(userService.getUserVO(user));
//        });
//        // endregion

        questionBankVOPage.setRecords(questionBankVOList);
        return questionBankVOPage;
    }
}




