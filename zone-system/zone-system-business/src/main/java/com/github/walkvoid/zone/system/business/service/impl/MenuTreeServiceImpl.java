package com.github.walkvoid.zone.system.business.service.impl;

import com.github.walkvoid.zone.system.api.service.MenuTreeService;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.system.model.query.MenuTreeQuery;
import com.github.walkvoid.zone.system.model.vo.MenuTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单树服务实现
 * 将扁平 Menu 列表递归构建为树形结构，适配前端 Vue Vben Admin 动态路由
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Service
public class MenuTreeServiceImpl implements MenuTreeService {

    /** 菜单类型：目录 */
    private static final String MENU_TYPE_DIR = "0";
    /** 菜单类型：菜单 */
    private static final String MENU_TYPE_MENU = "1";
    /** 菜单类型：按钮（不显示在菜单树中） */
    private static final String MENU_TYPE_BUTTON = "2";

    @Autowired
    private MenuDAO menuDAO;

    @Override
    public List<MenuTreeNode> getMenuTree(MenuTreeQuery query) {
        List<Menu> menus;
        if (Boolean.TRUE.equals(query.getOnlyEnabled())) {
            menus = menuDAO.selectAllEnabledMenus();
        } else {
            menus = menuDAO.selectAll();
        }

        // 过滤掉按钮类型
        menus = menus.stream()
                .filter(m -> !MENU_TYPE_BUTTON.equals(m.getMenuType()))
                .collect(Collectors.toList());

        // 如果只显示可见菜单
        if (Boolean.TRUE.equals(query.getOnlyVisible())) {
            menus = menus.stream()
                    .filter(m -> m.getVisible() == null || m.getVisible().getKey() != 0)
                    .collect(Collectors.toList());
        }

        return buildTree(menus);
    }

    @Override
    public List<MenuTreeNode> getAllMenuTree() {
        MenuTreeQuery query = new MenuTreeQuery();
        query.setOnlyEnabled(false);
        query.setOnlyVisible(false);
        return getMenuTree(query);
    }

    /**
     * 构建菜单树：按 parentId 分组，递归构建层级
     */
    private List<MenuTreeNode> buildTree(List<Menu> menus) {
        // 按 parentId 分组
        Map<Long, List<Menu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(
                        Menu::getParentId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 构建根节点（parentId = 0）
        List<Menu> roots = parentMap.getOrDefault(0L, Collections.emptyList());

        return roots.stream()
                .map(menu -> convertToNode(menu, parentMap))
                .collect(Collectors.toList());
    }

    /**
     * 递归转换 Menu → MenuTreeNode
     */
    private MenuTreeNode convertToNode(Menu menu, Map<Long, List<Menu>> parentMap) {
        MenuTreeNode node = new MenuTreeNode();

        // name: 路由名称（前端要求必填）
        node.setName(menu.getMenuCode());

        // path: 路由路径
        node.setPath(menu.getUrl());

        // component: 组件路径
        if (MENU_TYPE_DIR.equals(menu.getMenuType())) {
            // 目录类型：使用基础布局组件
            node.setComponent("BasicLayout");
        } else if (MENU_TYPE_MENU.equals(menu.getMenuType())) {
            // 菜单类型：构建 views 路径
            String url = menu.getUrl();
            if (StringUtils.hasText(url)) {
                node.setComponent("views" + url + "/index.vue");
            }
        }

        // meta: 元信息
        MenuTreeNode.MenuMeta meta = new MenuTreeNode.MenuMeta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setOrder(menu.getSort());
        // visible: YES=显示隐藏, hideInMenu 取反
        meta.setHideInMenu(menu.getVisible() != null && menu.getVisible().getKey() == 0);

        // 权限标识
        if (StringUtils.hasText(menu.getPermission())) {
            meta.setAuthority(Collections.singletonList(menu.getPermission()));
        }

        node.setMeta(meta);

        // 递归构建子节点
        List<Menu> children = parentMap.getOrDefault(menu.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<MenuTreeNode> childNodes = children.stream()
                    .map(child -> convertToNode(child, parentMap))
                    .collect(Collectors.toList());
            node.setChildren(childNodes);
        }

        return node;
    }
}
