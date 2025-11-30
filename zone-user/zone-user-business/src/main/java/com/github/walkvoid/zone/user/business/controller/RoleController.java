package com.github.walkvoid.zone.user.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.user.business.db.dao.RoleDAO;
import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.model.dto.RoleDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 角色Controller
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 角色相关接口
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleDAO roleDAO;

    /**
     * 新增角色
     * @param roleDTO 角色信息DTO
     * @return 新增结果
     */
    @PostMapping
    public boolean addRole(@RequestBody RoleDTO roleDTO) {
        if (Objects.isNull(roleDTO)) {
            return false;
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        return roleDAO.insert(role) > 0;
    }

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息DTO
     */
    @GetMapping("/{id}")
    public RoleDTO getRoleById(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        Role role = roleDAO.selectById(id);
        if (Objects.isNull(role)) {
            return null;
        }
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);
        return roleDTO;
    }

    /**
     * 根据角色代码查询角色
     * @param roleCode 角色代码
     * @return 角色信息DTO
     */
    @GetMapping("/code/{roleCode}")
    public RoleDTO getRoleByCode(@PathVariable("roleCode") String roleCode) {
        if (Objects.isNull(roleCode)) {
            return null;
        }
        Role role = roleDAO.selectByRoleCode(roleCode);
        if (Objects.isNull(role)) {
            return null;
        }
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);
        return roleDTO;
    }

    /**
     * 查询角色列表
     * @param roleDTO 查询条件
     * @return 角色列表DTO
     */
    @GetMapping("/list")
    public List<RoleDTO> getRoleList(RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        List<Role> roleList = roleDAO.selectList(role);
        List<RoleDTO> roleDTOList = new ArrayList<>();
        for (Role r : roleList) {
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(r, dto);
            roleDTOList.add(dto);
        }
        return roleDTOList;
    }

    /**
     * 更新角色信息
     * @param roleDTO 角色信息DTO
     * @return 更新结果
     */
    @PutMapping
    public boolean updateRole(@RequestBody RoleDTO roleDTO) {
        if (Objects.isNull(roleDTO) || Objects.isNull(roleDTO.getId())) {
            return false;
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        return roleDAO.updateById(role) > 0;
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteRole(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        return roleDAO.deleteById(id) > 0;
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestBody List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return false;
        }
        return roleDAO.deleteBatchIds(ids) > 0;
    }

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表DTO
     */
    @GetMapping("/by-user/{userId}")
    public List<RoleDTO> getRolesByUserId(@PathVariable("userId") Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<Role> roleList = roleDAO.selectRolesByUserId(userId);
        List<RoleDTO> roleDTOList = new ArrayList<>();
        for (Role role : roleList) {
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(role, dto);
            roleDTOList.add(dto);
        }
        return roleDTOList;
    }

    /**
     * 查询所有可用的角色
     * @return 角色列表DTO
     */
    @GetMapping("/available")
    public List<RoleDTO> getAvailableRoles() {
        List<Role> roleList = roleDAO.selectAvailableRoles();
        List<RoleDTO> roleDTOList = new ArrayList<>();
        for (Role role : roleList) {
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(role, dto);
            roleDTOList.add(dto);
        }
        return roleDTOList;
    }

    /**
     * 查询非系统角色
     * @return 角色列表DTO
     */
    @GetMapping("/non-system")
    public List<RoleDTO> getNonSystemRoles() {
        List<Role> roleList = roleDAO.selectNonSystemRoles();
        List<RoleDTO> roleDTOList = new ArrayList<>();
        for (Role role : roleList) {
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(role, dto);
            roleDTOList.add(dto);
        }
        return roleDTOList;
    }
}