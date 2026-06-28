package com.github.walkvoid.zone.system.business.service.impl;

import com.github.walkvoid.zone.system.api.service.MenuTreeService;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.dto.MenuTreeQueryDTO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.system.model.vo.MenuTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuTreeServiceImpl implements MenuTreeService {

    private static final String MENU_TYPE_DIR = "0";
    private static final String MENU_TYPE_MENU = "1";
    private static final String MENU_TYPE_BUTTON = "2";

    @Autowired
    private MenuDAO menuDAO;

    @Override
    public List<MenuTreeNode> getMenuTree(MenuTreeQueryDTO query) {
        List<Menu> menus;
        if (Boolean.TRUE.equals(query.getOnlyVisible())) {
            menus = menuDAO.selectAllVisibleMenus();
        } else {
            menus = menuDAO.selectAll();
        }

        menus = menus.stream()
                .filter(m -> !MENU_TYPE_BUTTON.equals(m.getMenuType()))
                .collect(Collectors.toList());

        return buildTree(menus);
    }

    @Override
    public List<MenuTreeNode> getAllMenuTree() {
        MenuTreeQueryDTO query = new MenuTreeQueryDTO();
        query.setOnlyVisible(false);
        return getMenuTree(query);
    }

    private List<MenuTreeNode> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));

        parentMap.values().forEach(this::sortMenusBySort);

        List<Menu> roots = parentMap.getOrDefault(0L, Collections.emptyList());

        return roots.stream()
                .map(menu -> convertToNode(menu, parentMap))
                .collect(Collectors.toList());
    }

    private void sortMenusBySort(List<Menu> menus) {
        menus.sort(Comparator
                .comparing((Menu menu) -> menu.getSort() != null ? menu.getSort() : 0)
                .thenComparing(menu -> menu.getId() != null ? menu.getId() : 0L));
    }

    private MenuTreeNode convertToNode(Menu menu, Map<Long, List<Menu>> parentMap) {
        MenuTreeNode node = new MenuTreeNode();

        node.setName(menu.getMenuCode());
        node.setPath(menu.getUrl());

        if (MENU_TYPE_DIR.equals(menu.getMenuType())) {
            node.setComponent("BasicLayout");
        } else if (MENU_TYPE_MENU.equals(menu.getMenuType())) {
            String url = menu.getUrl();
            if (StringUtils.hasText(url)) {
                node.setComponent(resolveViewComponent(url));
            }
        }

        MenuTreeNode.MenuMeta meta = new MenuTreeNode.MenuMeta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setOrder(menu.getSort() != null ? menu.getSort() : 0);
        meta.setHideInMenu(menu.getVisible() != null && menu.getVisible().getKey() == 0);

        if (StringUtils.hasText(menu.getPermission())) {
            meta.setAuthority(Collections.singletonList(menu.getPermission()));
        }

        node.setMeta(meta);

        List<Menu> children = parentMap.getOrDefault(menu.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<MenuTreeNode> childNodes = children.stream()
                    .map(child -> convertToNode(child, parentMap))
                    .collect(Collectors.toList());
            node.setChildren(childNodes);
        }

        return node;
    }

    private String resolveViewComponent(String url) {
        if ("/ai/ai-model".equals(url)) {
            return "views/ai/model/list.vue";
        }
        return "views" + url + "/list.vue";
    }
}
