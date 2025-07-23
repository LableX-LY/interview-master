package com.xly.interview.master.esdao;

import com.xly.interview.master.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/22 21:48
 * @description
 **/

public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    /**
     * 根据用户 id 查询
     * @param userId
     * @return
     */
    List<QuestionEsDTO> findByUserId(Long userId);

}
