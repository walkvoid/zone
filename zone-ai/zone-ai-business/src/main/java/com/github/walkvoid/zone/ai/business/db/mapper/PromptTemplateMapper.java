package com.github.walkvoid.zone.ai.business.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * Prompt模板 Mapper
 *
 * @author walkvoid
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
