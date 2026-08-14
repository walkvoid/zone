package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.business.db.mapper.McpServerConfigMapper;
import com.github.walkvoid.zone.ai.model.dto.McpServerConfigDTO;
import com.github.walkvoid.zone.ai.model.entity.McpServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MCP Server配置 DAO
 *
 * @author walkvoid
 */
@Repository
public class McpServerConfigDAO {

    @Autowired
    private McpServerConfigMapper mapper;

    public int insert(McpServerConfig entity) {
        return mapper.insert(entity);
    }

    public int updateById(McpServerConfig entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public McpServerConfig selectById(Long id) {
        return mapper.selectById(id);
    }

    public McpServerConfig selectByCode(String serverCode) {
        return mapper.selectOne(new QueryWrapper<McpServerConfig>()
                .eq("server_code", serverCode));
    }

    public List<McpServerConfig> selectEnabled() {
        return mapper.selectList(new QueryWrapper<McpServerConfig>()
                .eq("status", 1));
    }

    public List<McpServerConfig> selectRunning() {
        return mapper.selectList(new QueryWrapper<McpServerConfig>()
                .eq("running_status", 1));
    }

    public List<McpServerConfig> selectList(McpServerConfig condition) {
        return mapper.selectList(new QueryWrapper<>(condition)
                .orderByDesc("update_time"));
    }

    public PageDTO<McpServerConfigDTO> page(PageRequest<McpServerConfigDTO> pageRequest) {
        McpServerConfig condition = BeanCopyUtils.copyBean(pageRequest.getParam(), McpServerConfig.class);
        Page<McpServerConfig> mpPage = mapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                new QueryWrapper<>(condition).orderByDesc("update_time"));
        List<McpServerConfigDTO> records = BeanCopyUtils.copyList(mpPage.getRecords(), McpServerConfigDTO.class);
        PageDTO<McpServerConfigDTO> result = new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public int checkCodeExists(String serverCode) {
        return Math.toIntExact(mapper.selectCount(
                new QueryWrapper<McpServerConfig>().eq("server_code", serverCode)));
    }
}
