package com.github.walkvoid.zone.system.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import com.github.walkvoid.zone.system.api.service.MenuCrudService;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.dto.MenuDTO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.system.model.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统菜单 CRUD 服务实现
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Service
public class MenuCrudServiceImpl implements MenuCrudService {

    @Autowired
    private MenuDAO menuDAO;

    @Override
    public List<MenuVO> getMenuList() {
        List<Menu> menus = menuDAO.selectAll();
        // 转换为 VO 并按 pid 构建树
        List<MenuVO> vos = menus.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        // 构建树形结构
        Map<Long, List<MenuVO>> pidMap = vos.stream()
                .collect(Collectors.groupingBy(
                        vo -> vo.getPid() != null ? vo.getPid() : 0L,
                        LinkedHashMap::new, Collectors.toList()));

        List<MenuVO> roots = pidMap.getOrDefault(0L, Collections.emptyList());
        // 按 order 排序
        roots.sort(Comparator.comparing(vo -> vo.getMeta() != null ? vo.getMeta().getOrder() : 0));
        for (MenuVO root : roots) {
            buildChildren(root, pidMap);
        }
        return roots;
    }

    private void buildChildren(MenuVO parent, Map<Long, List<MenuVO>> pidMap) {
        List<MenuVO> children = pidMap.getOrDefault(parent.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            // 按 order 排序
            children.sort(Comparator.comparing(vo -> vo.getMeta() != null ? vo.getMeta().getOrder() : 0));
            parent.setChildren(children);
            for (MenuVO child : children) {
                buildChildren(child, pidMap);
            }
        }
    }

    @Override
    public void createMenu(MenuDTO dto) {
        Menu menu = dtoToEntity(dto);
        menuDAO.insert(menu);
    }

    @Override
    public void updateMenu(Long id, MenuDTO dto) {
        Menu existing = menuDAO.selectById(id);
        if (existing == null) {
            throw new RuntimeException("菜单不存在: " + id);
        }
        Menu menu = dtoToEntity(dto);
        menu.setId(id);
        menuDAO.updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        menuDAO.deleteById(id);
    }

    @Override
    public boolean isMenuNameExists(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) return false;
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("menu_code", name);
        if (excludeId != null && excludeId > 0) {
            wrapper.ne("id", excludeId);
        }
        return menuDAO.count(wrapper) > 0;
    }

    @Override
    public boolean isMenuPathExists(String path, Long excludeId) {
        if (!StringUtils.hasText(path)) return false;
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("url", path);
        if (excludeId != null && excludeId > 0) {
            wrapper.ne("id", excludeId);
        }
        return menuDAO.count(wrapper) > 0;
    }

    @Override
    public Map<String, Object> getMenuPage(int page, int size) {
        Page<Menu> pageParam = new Page<>(page, size);
        Page<Menu> result = menuDAO.selectPage(pageParam);
        List<MenuVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", result.getTotal());
        return map;
    }

    // ==================== 转换方法 ====================

    private MenuVO toVO(Menu entity) {
        MenuVO vo = new MenuVO();
        vo.setId(entity.getId());
        vo.setName(entity.getMenuCode());
        vo.setType(mapTypeReverse(entity.getMenuType()));
        vo.setPid(entity.getParentId());
        vo.setPath(entity.getUrl());
        vo.setAuthCode(entity.getPermission());
        vo.setStatus(entity.getStatus());

        MenuVO.MenuMeta meta = new MenuVO.MenuMeta();
        meta.setTitle(entity.getMenuName());
        meta.setIcon(entity.getIcon());
        meta.setOrder(entity.getSort());
        if (entity.getVisible() != null && entity.getVisible().getKey() == 0) {
            meta.setHideInMenu(true);
        }
        vo.setMeta(meta);

        return vo;
    }

    private Menu dtoToEntity(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setMenuCode(dto.getName());
        menu.setMenuType(mapType(dto.getType()));
        menu.setParentId(dto.getPid() != null ? dto.getPid() : 0L);
        menu.setUrl(dto.getPath());
        menu.setPermission(dto.getAuthCode());
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        // meta 映射
        if (dto.getMeta() != null) {
            menu.setMenuName(dto.getMeta().getTitle());
            menu.setIcon(dto.getMeta().getIcon());
            menu.setSort(dto.getMeta().getOrder());
            if (Boolean.TRUE.equals(dto.getMeta().getHideInMenu())) {
                menu.setVisible(BooleanEnum.NO);
            } else {
                menu.setVisible(BooleanEnum.YES);
            }
        }

        return menu;
    }

    /**
     * 前端 type → 数据库 menu_type
     */
    private String mapType(String type) {
        if (type == null) return "1";
        return switch (type) {
            case "catalog" -> "0";
            case "menu" -> "1";
            case "button" -> "2";
            case "embedded" -> "3";
            case "link" -> "4";
            default -> "1";
        };
    }

    /**
     * 数据库 menu_type → 前端 type
     */
    private String mapTypeReverse(String menuType) {
        if (menuType == null) return "menu";
        return switch (menuType) {
            case "0" -> "catalog";
            case "1" -> "menu";
            case "2" -> "button";
            case "3" -> "embedded";
            case "4" -> "link";
            default -> "menu";
        };
    }
}
