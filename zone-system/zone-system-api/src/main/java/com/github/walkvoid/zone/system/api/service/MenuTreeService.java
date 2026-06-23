package com.github.walkvoid.zone.system.api.service;

import com.github.walkvoid.zone.system.model.query.MenuTreeQuery;
import com.github.walkvoid.zone.system.model.vo.MenuTreeNode;

import java.util.List;

/**
 * 菜单树服务接口
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
public interface MenuTreeService {

    /**
     * 根据条件获取菜单树
     *
     * @param query 查询条件
     * @return 菜单树根节点列表
     */
    List<MenuTreeNode> getMenuTree(MenuTreeQuery query);

    /**
     * 获取所有菜单树（不过滤）
     *
     * @return 完整菜单树
     */
    List<MenuTreeNode> getAllMenuTree();
}
