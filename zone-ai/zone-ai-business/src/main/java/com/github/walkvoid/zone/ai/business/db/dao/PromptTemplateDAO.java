package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.business.db.mapper.PromptTemplateMapper;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
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

    public PageResponse<PromptTemplateDTO> page(PageRequest<PromptTemplateDTO> pageRequest) {
        PromptTemplate condition = BeanCopyUtils.copyBean(pageRequest.getParameter(), PromptTemplate.class);
        Page<PromptTemplate> page = mapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                new QueryWrapper<>(condition).orderByDesc("update_time"));
        List<PromptTemplateDTO> records = BeanCopyUtils.copyList(page.getRecords(), PromptTemplateDTO.class);
        return new PageResponse<>(page.getTotal(), (int) page.getSize(), page.getCurrent(), records);
    }

    public int checkCodeExists(String templateCode) {
        return Math.toIntExact(mapper.selectCount(
                new QueryWrapper<PromptTemplate>().eq("template_code", templateCode)));
    }
}
