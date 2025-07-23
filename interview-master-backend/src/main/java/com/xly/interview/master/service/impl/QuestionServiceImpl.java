package com.xly.interview.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.common.SqlUtils;
import com.xly.interview.master.constant.CommonConstant;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.model.bean.Question;
import com.xly.interview.master.model.bean.QuestionBankQuestion;
import com.xly.interview.master.model.dto.question.QuestionAddRequest;
import com.xly.interview.master.model.dto.question.QuestionEsDTO;
import com.xly.interview.master.model.dto.question.QuestionQueryRequest;
import com.xly.interview.master.model.vo.question.QuestionVO;
import com.xly.interview.master.service.QuestionBankQuestionService;
import com.xly.interview.master.service.QuestionService;
import com.xly.interview.master.mapper.QuestionMapper;
import com.xly.interview.master.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author x-ly
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {

        int current = questionQueryRequest.getCurrent();
        int pageSize = questionQueryRequest.getPageSize();
        // 构建QueryWrapper
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        // 题库ID
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (ObjectUtils.isNotEmpty(questionBankId)) {
            // 存在题库ID，则先查询对应题库ID的题目
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            List<QuestionBankQuestion> questionList = questionBankQuestionService.list(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(questionList)) {
                // 取出题目 id 集合
                Set<Long> questionIdSet = questionList.stream()
                        .map(QuestionBankQuestion::getQuestionId)
                        .collect(Collectors.toSet());
                // 复用原有题目表的查询条件
                queryWrapper.in("id", questionIdSet);
            } else {
                // 题库为空，则返回空列表
                return new Page<>(current, pageSize, 0);
            }
        }
        // 题库ID为空，直接查询数据库，无需查询管理查询题目题库关联表
        Page<Question> questionPage = this.page(new Page<>(current, pageSize), queryWrapper);
        return questionPage;
    }

    @Override
    public Long addQuestion(QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        String title = questionAddRequest.getTitle();
        String content = questionAddRequest.getContent();
        List<String> tags = questionAddRequest.getTags();
        String answer = questionAddRequest.getAnswer();
        //构建实体类
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setAnswer(answer);
        if (tags != null && !tags.isEmpty()) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        this.validQuestion(question,true);
        question.setUserId(userService.getloginUser(request).getId());
        int insert = questionMapper.insert(question);
        if (insert < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统内部异常，题目添加失败!");
        }
        return question.getId();
    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        String answer = questionQueryRequest.getAnswer();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空!");
        }
        // todo 从对象中取值
        String title = question.getTitle();
        String content = question.getContent();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            if (StringUtils.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空!");
            }
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            if (title.length() > 80) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题长度须小于80!");
            }
        }
        if (StringUtils.isNotBlank(content)) {
            if (content.length() > 10240) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长!");
            }
        }
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> records = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(),
                questionPage.getSize(),
                questionPage.getTotal());
        if (CollUtil.isEmpty(records)) {
            return questionVOPage;
        }
        List<QuestionVO> collect = records.stream().
                map(QuestionVO::objToVo).
                collect(Collectors.toList());
        questionVOPage.setRecords(collect);
        return questionVOPage;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = new QuestionVO();
        BeanUtil.copyProperties(question, questionVO);
        String tags = question.getTags();
        if (StringUtils.isNotBlank(tags)) {
            questionVO.setTagList(JSONUtil.toList(tags, String.class));
        }
        return questionVO;
    }

    @Override
    public Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest) {
        // 获取参数
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String searchText = questionQueryRequest.getSearchText();
        List<String> tags = questionQueryRequest.getTags();
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        Long userId = questionQueryRequest.getUserId();
        // 注意，ES 的起始页为 0
        int current = questionQueryRequest.getCurrent() - 1;
        int pageSize = questionQueryRequest.getPageSize();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 构造查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        if (questionBankId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("questionBankId", questionBankId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("answer", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of(current, pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSorts(sortBuilder)
                .build();
        SearchHits<QuestionEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, QuestionEsDTO.class);
        // 复用 MySQL 的分页对象，封装返回结果
        Page<Question> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Question> resourceList = new ArrayList<>();
        if (searchHits.hasSearchHits()) {
            List<SearchHit<QuestionEsDTO>> searchHitList = searchHits.getSearchHits();
            for (SearchHit<QuestionEsDTO> questionEsDTOSearchHit : searchHitList) {
                resourceList.add(QuestionEsDTO.dtoToObj(questionEsDTOSearchHit.getContent()));
            }
        }
        page.setRecords(resourceList);
        return page;
    }
}




