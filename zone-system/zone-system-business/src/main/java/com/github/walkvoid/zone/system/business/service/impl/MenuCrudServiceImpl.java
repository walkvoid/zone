package com.github.walkvoid.zone.system.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.zone.system.api.service.MenuCrudService;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.dto.MenuDTO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.user.business.db.dao.RoleMenuRelDAO;
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

    @Autowired
    private RoleMenuRelDAO roleMenuRelDAO;

    @Override
    public List<MenuDTO> getMenuList() {
        List<Menu> menus = menuDAO.selectAll();
        List<MenuDTO> dtos = menus.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Map<Long, List<MenuDTO>> pidMap = dtos.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getPid() != null ? dto.getPid() : 0L,
                        LinkedHashMap::new, Collectors.toList()));

        List<MenuDTO> roots = pidMap.getOrDefault(0L, Collections.emptyList());
        roots.sort(Comparator.comparing(this::resolveSort));
        for (MenuDTO root : roots) {
            buildChildren(root, pidMap);
        }
        return roots;
    }

    private void buildChildren(MenuDTO parent, Map<Long, List<MenuDTO>> pidMap) {
        List<MenuDTO> children = pidMap.getOrDefault(parent.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            children.sort(Comparator.comparing(this::resolveSort));
            parent.setChildren(children);
            for (MenuDTO child : children) {
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
        if (id == null || id <= 0) {
            throw new RuntimeException("菜单ID无效");
        }
        Menu existing = menuDAO.selectById(id);
        if (existing == null) {
            throw new RuntimeException("菜单不存在: " + id);
        }
        if (menuDAO.countByParentId(id) > 0) {
            throw new RuntimeException("存在子菜单，无法删除");
        }
        roleMenuRelDAO.deleteByMenuId(id);
        if (menuDAO.deleteById(id) <= 0) {
            throw new RuntimeException("删除菜单失败: " + id);
        }
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
    public PageResponse<MenuDTO> page(PageRequest<Void> pageRequest) {
        Page<Menu> result = menuDAO.selectPage(
                new PageDTO<>(pageRequest.getCurrent(), pageRequest.getSize()));
        List<MenuDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageResponse<>(result.getTotal(), (int) result.getSize(), result.getCurrent(), records);
    }

    // ==================== 转换方法 ====================

    private MenuDTO toDTO(Menu entity) {
        MenuDTO dto = new MenuDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getMenuCode());
        dto.setType(mapTypeReverse(entity.getMenuType()));
        dto.setPid(entity.getParentId());
        dto.setPath(entity.getUrl());
        dto.setAuthCode(entity.getPermission());
        dto.setVisible(resolveVisible(entity));
        dto.setSort(entity.getSort() != null ? entity.getSort() : 0);

        MenuDTO.MenuMeta meta = new MenuDTO.MenuMeta();
        meta.setTitle(entity.getMenuName());
        meta.setIcon(entity.getIcon());
        meta.setOrder(dto.getSort());
        dto.setMeta(meta);

        return dto;
    }

    private int resolveVisible(Menu entity) {
        return entity.getVisible() != null && entity.getVisible().getKey() == 0 ? 0 : 1;
    }

    private Menu dtoToEntity(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setMenuCode(dto.getName());
        menu.setMenuType(mapType(dto.getType()));
        menu.setParentId(dto.getPid() != null ? dto.getPid() : 0L);
        menu.setUrl(dto.getPath());
        menu.setPermission(dto.getAuthCode());
        menu.setSort(resolveSort(dto));
        menu.setVisible(resolveVisible(dto));

        if (dto.getMeta() != null) {
            menu.setMenuName(dto.getMeta().getTitle());
            menu.setIcon(dto.getMeta().getIcon());
        }

        return menu;
    }

    private BooleanEnum resolveVisible(MenuDTO dto) {
        if (dto.getVisible() != null) {
            return dto.getVisible() == 0 ? BooleanEnum.NO : BooleanEnum.YES;
        }
        // 兼容旧数据：meta.hideInMenu
        if (dto.getMeta() != null && Boolean.TRUE.equals(dto.getMeta().getHideInMenu())) {
            return BooleanEnum.NO;
        }
        return BooleanEnum.YES;
    }

    private int resolveSort(MenuDTO dto) {
        if (dto.getSort() != null) {
            return dto.getSort();
        }
        if (dto.getMeta() != null && dto.getMeta().getOrder() != null) {
            return dto.getMeta().getOrder();
        }
        return 0;
    }

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
