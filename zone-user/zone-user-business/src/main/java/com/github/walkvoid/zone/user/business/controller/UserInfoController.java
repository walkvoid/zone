package com.github.walkvoid.zone.user.business.controller;

import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户信息Controller
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户信息相关接口
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Autowired
    private UserInfoDAO userInfoDAO;

    /**
     * 新增用户
     * @param userInfoDTO 用户信息DTO
     * @return 新增结果
     */
    @PostMapping
    public boolean addUser(@RequestBody UserInfoDTO userInfoDTO) {
        if (Objects.isNull(userInfoDTO)) {
            return false;
        }
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        return userInfoDAO.insert(userInfo) > 0;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息DTO
     */
    @GetMapping("/{id}")
    public UserInfoDTO getUserById(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        UserInfo userInfo = userInfoDAO.selectById(id);
        if (Objects.isNull(userInfo)) {
            return null;
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(userInfo, userInfoDTO);
        return userInfoDTO;
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息DTO
     */
    @GetMapping("/username/{username}")
    public UserInfoDTO getUserByUsername(@PathVariable("username") String username) {
        if (Objects.isNull(username)) {
            return null;
        }
        UserInfo userInfo = userInfoDAO.selectByUsername(username);
        if (Objects.isNull(userInfo)) {
            return null;
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(userInfo, userInfoDTO);
        return userInfoDTO;
    }

    /**
     * 查询用户列表
     * @param userInfoDTO 查询条件
     * @return 用户列表DTO
     */
    @GetMapping("/list")
    public List<UserInfoDTO> getUserList(UserInfoDTO userInfoDTO) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        List<UserInfo> userInfoList = userInfoDAO.selectList(userInfo);
        List<UserInfoDTO> userInfoDTOList = new ArrayList<>();
        for (UserInfo info : userInfoList) {
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(info, dto);
            userInfoDTOList.add(dto);
        }
        return userInfoDTOList;
    }

    /**
     * 更新用户信息
     * @param userInfoDTO 用户信息DTO
     * @return 更新结果
     */
    @PutMapping
    public boolean updateUser(@RequestBody UserInfoDTO userInfoDTO) {
        if (Objects.isNull(userInfoDTO) || Objects.isNull(userInfoDTO.getId())) {
            return false;
        }
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        return userInfoDAO.updateById(userInfo) > 0;
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        return userInfoDAO.deleteById(id) > 0;
    }

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestBody List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return false;
        }
        return userInfoDAO.deleteBatchIds(ids) > 0;
    }
}