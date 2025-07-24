package com.xly.interview.master.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xly.interview.master.common.ErrorCode;
import com.xly.interview.master.exception.BusinessException;
import com.xly.interview.master.mapper.QuestionBankMapper;
import com.xly.interview.master.model.bean.Question;
import com.xly.interview.master.model.bean.QuestionBank;
import com.xly.interview.master.model.bean.QuestionBankQuestion;
import com.xly.interview.master.model.bean.User;
import com.xly.interview.master.service.QuestionBankQuestionService;
import com.xly.interview.master.mapper.QuestionBankQuestionMapper;
import com.xly.interview.master.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author x-ly
* @description 针对表【question_bank_question(题库题目)】的数据库操作Service实现
* @createDate 2025-07-03 15:02:22
*/
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion>
    implements QuestionBankQuestionService{

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionBankMapper questionBankMapper;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBank(Long questionBankId, List<Long> questionIds, User loginUser) {
        // 1.参数校验
        if (questionBankId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"题库ID错误!");
        }
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"题目列表为空!");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"请先登录!");
        }
        // 2.检查题库是否存在
        QuestionBank questionBank = questionBankMapper.selectById(questionBankId);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"指定题库不存在!");
        }
        // 3.检查题目ID是否存在
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIds);
        // 合法的题目 id 列表
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper, obj -> (Long) obj);
        if (CollUtil.isEmpty(validQuestionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "合法的题目 id 列表为空");
        }
//        List<Question> questions = questionService.listByIds(questionIds);
//        List<Long> validQuestionIdList = questions.stream()
//                .map(Question::getId)
//                .collect(Collectors.toList());
//        if (CollUtil.isEmpty(validQuestionIdList)) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"合法的题目列表为空!");
//        }

        // 4.排除已经和题库绑定的题库ID
        // 检查哪些题目还不存在于题库中，避免重复插入
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);
        // 已存在于题库中的题目 id
        Set<Long> existQuestionIdSet = existQuestionList.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toSet());
        // 已存在于题库中的题目 id，不需要再次添加
        validQuestionIdList = validQuestionIdList.stream().filter(questionId -> {
            return !existQuestionIdSet.contains(questionId);
        }).collect(Collectors.toList());
        if(CollUtil.isEmpty(validQuestionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "所有题目都已存在于题库中");
        }

        // 自定义线程池（IO 密集型线程池）
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                20,             // 核心线程数
                50,                        // 最大线程数
                60L,                       // 线程空闲存活时间
                TimeUnit.SECONDS,           // 存活时间单位
                new LinkedBlockingQueue<>(10000),  // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理任务
        );

        // 保存所有批次任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 分批处理，避免长事务，假设每次处理 1000 条数据
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIdList.size();
        for (int i = 0; i < totalQuestionListSize; i += batchSize) {
            // 生成每批次的数据
            List<Long> subList = validQuestionIdList.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream()
                    .map(questionId -> {
                        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                        questionBankQuestion.setQuestionBankId(questionBankId);
                        questionBankQuestion.setQuestionId(questionId);
                        questionBankQuestion.setUserId(loginUser.getId());
                        return questionBankQuestion;
                    }).collect(Collectors.toList());
            // 使用事务处理每批数据
            // 获取代理
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();

            // 异步处理每批数据，将任务添加到异步任务列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions);
            }, customExecutor);
            futures.add(future);
        }
        // 等待所有批次完成操作
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 关闭线程池
        customExecutor.shutdown();

//        // 分批处理避免长事务，假设每次处理 1000 条数据
//        int batchSize = 1000;
//        int totalQuestionListSize = validQuestionIdList.size();
//        for (int i = 0; i < totalQuestionListSize; i += batchSize) {
//            // 生成每批次的数据
//            List<Long> subList = validQuestionIdList.subList(i, Math.min(i + batchSize, totalQuestionListSize));
//            List<QuestionBankQuestion> questionBankQuestions = subList.stream().map(questionId -> {
//                QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
//                questionBankQuestion.setQuestionBankId(questionBankId);
//                questionBankQuestion.setQuestionId(questionId);
//                questionBankQuestion.setUserId(loginUser.getId());
//                return questionBankQuestion;
//            }).collect(Collectors.toList());
//            // 使用事务处理每批数据
//            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
//            questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions);
//        }

//        // 5..执行插入
//        for (Long questionId : validQuestionIdList) {
//            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
//            questionBankQuestion.setQuestionId(questionId);
//            questionBankQuestion.setQuestionBankId(questionBankId);
//            questionBankQuestion.setUserId(loginUser.getId());
//            int insert = questionBankQuestionMapper.insert(questionBankQuestion);
//            if (insert < 0) {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"向题库中添加题目失败!");
//            }
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId) {
        // 参数校验
        if (questionIdList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目列表为空");
        }
        if(questionBankId == null || questionBankId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库非法");
        }
        // 执行删除关联
        for (Long questionId : questionIdList) {
            // 构造查询
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean result = this.remove(lambdaQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "从题库移除题目失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteQuestions(List<Long> questionIdList) {
        if (CollUtil.isEmpty(questionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "要删除的题目列表为空");
        }
        for (Long questionId : questionIdList) {
            boolean result = questionService.removeById(questionId);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除题目失败");
            }
            // 移除题目题库关系
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId);
            result = this.remove(lambdaQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除题目题库关联失败");
            }
        }
    }

    /**
     * 批量添加题目到题库（事务，仅供内部调用）
     * @param questionBankQuestions
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        try {
            boolean result = this.saveBatch(questionBankQuestions);
            if(!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
            }
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }

}




