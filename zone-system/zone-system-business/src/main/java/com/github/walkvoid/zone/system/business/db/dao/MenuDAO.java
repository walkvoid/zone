package com.github.walkvoid.zone.system.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /** 统计 */
    public long count(QueryWrapper<Menu> wrapper) {
        return menuMapper.selectCount(wrapper);
    }

    /** 查询所有有效菜单（status=1），按排序号升序 */
    public List<Menu> selectAllEnabledMenus() {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort");
        return menuMapper.selectList(wrapper);
    }

    /**
     * 查询所有菜单，按排序号升序
     */
    public List<Menu> selectAll() {
        return menuMapper.selectList(new QueryWrapper<Menu>().orderByAsc("sort"));
    }

    /** 分页查询所有菜单，按 parent_id 和 sort 升序 */
    public Page<Menu> selectPage(Page<Menu> page) {
        return menuMapper.selectPage(page, new QueryWrapper<Menu>().orderByAsc("parent_id", "sort"));
    }
}
