package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.wvframework.models.BaseVO;
import com.github.walkvoid.zone.common.model.BaseEntity;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import com.github.walkvoid.zone.user.model.enums.UserGenderEnum;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户信息实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_info")
public class UserInfo extends BaseVO {
    private static final long serialVersionUID = -2711760906199432073L;


    /**
     * 用户ID，主键
     */
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 手机号（用于手机号登录）
     */
    private String phone;

    /**
     * 邮箱（用于邮箱登录）
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private UserGenderEnum gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    /**
     * 用户状态：
     */
    private UserInfoStatusEnum status;


    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 是否是管理员
     */
    private BooleanEnum isAdmin;



}
