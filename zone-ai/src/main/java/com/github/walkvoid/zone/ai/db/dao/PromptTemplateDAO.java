package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.db.mapper.PromptTemplateMapper;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Prompt模板 DAO
 *
 * @author walkvoid
 */
@Repository
public class PromptTemplateDAO {

    @Autowired
    private PromptTemplateMapper mapper;

    public int insert(PromptTemplate entity) {
        return mapper.insert(entity);
    }

    public int updateById(PromptTemplate entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public PromptTemplate selectById(Long id) {
        return mapper.selectById(id);
    }

    public PromptTemplate selectByCode(String templateCode) {
        return mapper.selectOne(new QueryWrapper<PromptTemplate>()
                .eq("template_code", templateCode));
    }

    public List<PromptTemplate> selectList(PromptTemplate condition) {
        QueryWrapper<PromptTemplate> qw = new QueryWrapper<>(condition);
        qw.orderByDesc("update_time");
        return mapper.selectList(qw);
    }

    public PageDTO<PromptTemplateDTO> page(PageRequest<PromptTemplateDTO> pageRequest) {
        PromptTemplateDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<PromptTemplate> qw = new QueryWrapper<PromptTemplate>().orderByDesc("update_time");
        if (param != null) {
            if (param.getTemplateName() != null && !param.getTemplateName().isBlank()) {
                qw.and(w -> w.like("template_name", param.getTemplateName().trim())
                        .or()
                        .like("template_code", param.getTemplateName().trim()));
            }
            if (param.getTemplateCode() != null && !param.getTemplateCode().isBlank()
                    && (param.getTemplateName() == null || param.getTemplateName().isBlank())) {
                qw.like("template_code", param.getTemplateCode().trim());
            }
            if (param.getCategory() != null && !param.getCategory().isBlank()) {
                qw.eq("category", param.getCategory().trim());
            }
            if (param.getStatus() != null) {
                qw.eq("status", param.getStatus());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<PromptTemplate> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<PromptTemplateDTO> records = BeanCopyUtils.copyList(mpPage.getRecords(), PromptTemplateDTO.class);
        PageDTO<PromptTemplateDTO> result = new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public int checkCodeExists(String templateCode) {
        return Math.toIntExact(mapper.selectCount(
                new QueryWrapper<PromptTemplate>().eq("template_code", templateCode)));
    }
}
