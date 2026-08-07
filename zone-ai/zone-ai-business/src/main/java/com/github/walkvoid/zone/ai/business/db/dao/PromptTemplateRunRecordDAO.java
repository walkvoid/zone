package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.business.db.mapper.PromptTemplateRunRecordMapper;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateRunRecordDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PromptTemplate运行记录 DAO
 *
 * @author walkvoid
 */
@Repository
public class PromptTemplateRunRecordDAO {

    @Autowired
    private PromptTemplateRunRecordMapper mapper;

    public int insert(PromptTemplateRunRecord entity) {
        return mapper.insert(entity);
    }

    public PromptTemplateRunRecord selectById(Long id) {
        return mapper.selectById(id);
    }

    public List<PromptTemplateRunRecord> selectByTemplateId(Long templateId) {
        return mapper.selectList(new QueryWrapper<PromptTemplateRunRecord>()
                .eq("template_id", templateId)
                .orderByDesc("create_time"));
    }

    public PageResponse<PromptTemplateRunRecordDTO> page(PageRequest<PromptTemplateRunRecordDTO> pageRequest) {
        PromptTemplateRunRecord condition = BeanCopyUtils.copyBean(pageRequest.getParam(), PromptTemplateRunRecord.class);
        Page<PromptTemplateRunRecord> page = mapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                new QueryWrapper<>(condition).orderByDesc("create_time"));
        List<PromptTemplateRunRecordDTO> records = BeanCopyUtils.copyList(page.getRecords(), PromptTemplateRunRecordDTO.class);
        return new PageResponse<>(page.getTotal(), (int) page.getSize(), page.getCurrent(), records);
    }
}
