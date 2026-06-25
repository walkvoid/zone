-- ============================================
-- Phase 1: 用户 + RBAC 完整建表 & 初始化
-- v3: 无 BaseEntity 继承，无 enable（走物理删除）
-- ============================================

-- ============================================
-- 1. 用户表 (user_info)
-- ============================================
CREATE TABLE IF NOT EXISTS `user_info` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`       VARCHAR(64)  NOT NULL                COMMENT '用户名',
    `password`       VARCHAR(128) NOT NULL                COMMENT '密码（BCrypt 加密）',
    `phone`          VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `email`          VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
    `nickname`       VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
    `avatar`         VARCHAR(256) DEFAULT NULL            COMMENT '头像',
    `gender`         INT          DEFAULT 0               COMMENT '性别：0-未知，1-男，2-女',
    `birthday`       DATETIME     DEFAULT NULL            COMMENT '生日',
    `status`         INT          DEFAULT 0               COMMENT '用户状态',
    `last_login_time` DATETIME    DEFAULT NULL            COMMENT '最后登录时间',
    `last_login_ip`  VARCHAR(64)  DEFAULT NULL            COMMENT '最后登录IP',
    `is_admin`       TINYINT      DEFAULT 0               COMMENT '是否管理员：1-是，0-否',
    `create_id`      BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`      BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ============================================
-- 2. 用户-角色关联表 (user_role_rel) — 纯关联，无审计字段
-- ============================================
CREATE TABLE IF NOT EXISTS `user_role_rel` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id` BIGINT NOT NULL                COMMENT '用户ID',
    `role_id` BIGINT NOT NULL                COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- ============================================
-- 3. 角色表 (role)
-- ============================================
CREATE TABLE IF NOT EXISTS `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色编码',
    `role_name`   VARCHAR(128) NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(256) DEFAULT NULL            COMMENT '角色描述',
    `is_system`   TINYINT      DEFAULT 0               COMMENT '是否系统角色：0-否，1-是',
    `create_id`   BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`   BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 4. 角色-菜单关联表 (role_menu_rel) — 纯关联，无审计字段
-- ============================================
CREATE TABLE IF NOT EXISTS `role_menu_rel` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id` BIGINT NOT NULL                COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL                COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- ============================================
-- 5. 初始角色数据
-- ============================================
INSERT INTO `role` (`role_code`, `role_name`, `description`, `is_system`) VALUES
('ROLE_ADMIN',  '超级管理员', '系统最高权限角色', 1),
('ROLE_USER',   '普通用户',   '基础用户角色',     0)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- ============================================
-- 6. 初始用户数据（密码 BCrypt 在 Java 端生成后更新）
-- ============================================
INSERT INTO `user_info` (`username`, `password`, `nickname`, `email`, `is_admin`, `status`) VALUES
('admin', 'CHANGE_ME_TO_BCRYPT', '管理员', 'admin@zone.com', 1, 1)
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`);

-- ============================================
-- 7. admin 用户绑定超级管理员角色
-- ============================================
INSERT INTO `user_role_rel` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user_info` u, `role` r
WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM `user_role_rel` urr
    WHERE urr.user_id = u.id AND urr.role_id = r.id
);

-- ============================================
-- 8. 角色-菜单关联：admin 角色拥有所有有权限码的菜单
-- ============================================
INSERT INTO `role_menu_rel` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM `role` r, `menu` m
WHERE r.role_code = 'ROLE_ADMIN' AND m.permission IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM `role_menu_rel` rmr
    WHERE rmr.role_id = r.id AND rmr.menu_id = m.id
);
