package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.ai.business.db.mapper.AiModelMapper;
import com.github.walkvoid.zone.ai.model.entity.AiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI模型 DAO
 *
 * @author walkvoid
 */
@Repository
public class AiModelDAO {

    @Autowired
    private AiModelMapper mapper;

    public int insert(AiModel model) {
        return mapper.insert(model);
    }

    public int updateById(AiModel model) {
        return mapper.updateById(model);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public AiModel selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiModel selectByCode(String modelCode) {
        return mapper.selectOne(new QueryWrapper<AiModel>()
                .eq("model_code", modelCode));
    }

    public List<AiModel> selectEnabled() {
        return mapper.selectList(new QueryWrapper<AiModel>()
                .eq("is_enabled", 1)
                .orderByDesc("priority"));
    }

    public List<AiModel> selectList(AiModel condition) {
        QueryWrapper<AiModel> qw = new QueryWrapper<>(condition);
        qw.orderByDesc("priority").orderByDesc("update_time");
        return mapper.selectList(qw);
    }

    public int checkCodeExists(String modelCode) {
        return Math.toIntExact(mapper.selectCount(
                new QueryWrapper<AiModel>().eq("model_code", modelCode)));
    }

    /** 调用次数 +1 */
    public int incrementCallCount(Long id) {
        AiModel model = mapper.selectById(id);
        if (model == null || model.getCallCount() == null) return 0;
        AiModel update = new AiModel();
        update.setId(id);
        update.setCallCount(model.getCallCount() + 1);
        return mapper.updateById(update);
    }
}
