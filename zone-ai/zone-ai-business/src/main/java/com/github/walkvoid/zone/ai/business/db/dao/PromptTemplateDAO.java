package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.ai.business.db.mapper.PromptTemplateMapper;
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

    public int checkCodeExists(String templateCode) {
        return Math.toIntExact(mapper.selectCount(
                new QueryWrapper<PromptTemplate>().eq("template_code", templateCode)));
    }
}
