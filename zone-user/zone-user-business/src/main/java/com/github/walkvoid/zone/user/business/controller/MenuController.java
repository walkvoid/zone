package com.github.walkvoid.zone.user.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.user.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.user.model.entity.Menu;
import com.github.walkvoid.zone.user.model.dto.MenuDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 菜单Controller
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 菜单相关接口
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuDAO menuDAO;

    /**
     * 新增菜单
     * @param menuDTO 菜单信息DTO
     * @return 新增结果
     */
    @PostMapping
    public boolean addMenu(@RequestBody MenuDTO menuDTO) {
        if (Objects.isNull(menuDTO)) {
            return false;
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDTO, menu);
        return menuDAO.insert(menu) > 0;
    }

    /**
     * 根据ID查询菜单
     * @param id 菜单ID
     * @return 菜单信息DTO
     */
    @GetMapping("/{id}")
    public MenuDTO getMenuById(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        Menu menu = menuDAO.selectById(id);
        if (Objects.isNull(menu)) {
            return null;
        }
        MenuDTO menuDTO = new MenuDTO();
        BeanUtils.copyProperties(menu, menuDTO);
        return menuDTO;
    }

    /**
     * 查询菜单列表
     * @param menuDTO 查询条件
     * @return 菜单列表DTO
     */
    @GetMapping("/list")
    public List<MenuDTO> getMenuList(MenuDTO menuDTO) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDTO, menu);
        List<Menu> menuList = menuDAO.selectList(menu);
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu m : menuList) {
            MenuDTO dto = new MenuDTO();
            BeanUtils.copyProperties(m, dto);
            menuDTOList.add(dto);
        }
        return menuDTOList;
    }

    /**
     * 更新菜单信息
     * @param menuDTO 菜单信息DTO
     * @return 更新结果
     */
    @PutMapping
    public boolean updateMenu(@RequestBody MenuDTO menuDTO) {
        if (Objects.isNull(menuDTO) || Objects.isNull(menuDTO.getId())) {
            return false;
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDTO, menu);
        return menuDAO.updateById(menu) > 0;
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteMenu(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        return menuDAO.deleteById(id) > 0;
    }

    /**
     * 批量删除菜单
     * @param ids 菜单ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public boolean deleteByIds(@RequestBody List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return false;
        }
        return menuDAO.deleteByIds(ids) > 0;
    }

    /**
     * 根据角色ID查询菜单列表
     * @param roleId 角色ID
     * @return 菜单列表DTO
     */
    @GetMapping("/by-role/{roleId}")
    public List<MenuDTO> getMenusByRoleId(@PathVariable("roleId") Long roleId) {
        if (Objects.isNull(roleId)) {
            return null;
        }
        List<Menu> menuList = menuDAO.selectMenusByRoleId(roleId);
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuList) {
            MenuDTO dto = new MenuDTO();
            BeanUtils.copyProperties(menu, dto);
            menuDTOList.add(dto);
        }
        return menuDTOList;
    }

    /**
     * 根据用户ID查询菜单列表
     * @param userId 用户ID
     * @return 菜单列表DTO
     */
    @GetMapping("/by-user/{userId}")
    public List<MenuDTO> getMenusByUserId(@PathVariable("userId") Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<Menu> menuList = menuDAO.selectMenusByUserId(userId);
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuList) {
            MenuDTO dto = new MenuDTO();
            BeanUtils.copyProperties(menu, dto);
            menuDTOList.add(dto);
        }
        return menuDTOList;
    }

    /**
     * 根据父菜单ID查询子菜单
     * @param parentId 父菜单ID
     * @return 子菜单列表DTO
     */
    @GetMapping("/child/{parentId}")
    public List<MenuDTO> getChildMenusByParentId(@PathVariable("parentId") Long parentId) {
        if (Objects.isNull(parentId)) {
            return null;
        }
        List<Menu> menuList = menuDAO.selectChildMenusByParentId(parentId);
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuList) {
            MenuDTO dto = new MenuDTO();
            BeanUtils.copyProperties(menu, dto);
            menuDTOList.add(dto);
        }
        return menuDTOList;
    }

    /**
     * 查询所有启用的菜单
     * @return 菜单列表DTO
     */
    @GetMapping("/enabled")
    public List<MenuDTO> getAllEnabledMenus() {
        List<Menu> menuList = menuDAO.selectAllEnabledMenus();
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuList) {
            MenuDTO dto = new MenuDTO();
            BeanUtils.copyProperties(menu, dto);
            menuDTOList.add(dto);
        }
        return menuDTOList;
    }
}