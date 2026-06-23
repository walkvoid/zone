-- ============================================
-- Zone 用户模块初始化数据
-- ============================================

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色编码',
    `role_name`   VARCHAR(128) NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(256) DEFAULT NULL            COMMENT '角色描述',
    `is_system`   TINYINT      DEFAULT 0               COMMENT '是否系统角色：0-否，1-是',
    `enable`      TINYINT      DEFAULT 1               COMMENT '启用状态：0-禁用，1-启用',
    `create_id`   BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`   BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu_rel` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `menu_id`     BIGINT   NOT NULL                COMMENT '菜单ID',
    `enable`      TINYINT  DEFAULT 1               COMMENT '启用状态',
    `create_id`   BIGINT   DEFAULT NULL            COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`   BIGINT   DEFAULT NULL            COMMENT '更新者ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- 初始角色数据
INSERT INTO `role` (`role_code`, `role_name`, `description`, `is_system`, `enable`) VALUES
('ROLE_ADMIN',  '超级管理员', '系统最高权限角色', 1, 1),
('ROLE_USER',   '普通用户',   '基础用户角色',     0, 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);
