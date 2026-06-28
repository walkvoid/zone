package com.github.walkvoid.zone.system.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.zone.system.business.db.mapper.MenuMapper;
import com.github.walkvoid.zone.system.model.entity.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统菜单 DAO
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Repository
public class MenuDAO {

    @Autowired
    private MenuMapper menuMapper;

    /** 根据ID查询 */
    public Menu selectById(Long id) {
        return menuMapper.selectById(id);
    }

    /** 插入 */
    public int insert(Menu menu) {
        return menuMapper.insert(menu);
    }

    /** 更新 */
    public int updateById(Menu menu) {
        return menuMapper.updateById(menu);
    }

    /** 删除 */
    public int deleteById(Long id) {
        return menuMapper.deleteById(id);
    }

    /** 统计子菜单数量 */
    public long countByParentId(Long parentId) {
        return menuMapper.selectCount(new QueryWrapper<Menu>().eq("parent_id", parentId));
    }

    /** 统计 */
    public long count(QueryWrapper<Menu> wrapper) {
        return menuMapper.selectCount(wrapper);
    }

    /** 查询所有可见菜单（visible=1），按排序号升序 */
    public List<Menu> selectAllVisibleMenus() {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("visible", 1).orderByAsc("parent_id", "sort", "id");
        return menuMapper.selectList(wrapper);
    }

    /**
     * 查询所有菜单，按排序号升序
     */
    public List<Menu> selectAll() {
        return menuMapper.selectList(new QueryWrapper<Menu>()
                .orderByAsc("parent_id", "sort", "id"));
    }

    /** 分页查询所有菜单，按 parent_id 和 sort 升序 */
    public Page<Menu> selectPage(PageDTO<Menu> page) {
        return menuMapper.selectPage(page, new QueryWrapper<Menu>().orderByAsc("parent_id", "sort"));
    }

    /** 根据ID列表查询 */
    public List<Menu> selectBatchIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return menuMapper.selectBatchIds(ids);
    }
}
