-- 菜单表（zone-system 模块）
-- 单表 + 代码递归构建树

DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '菜单ID，主键',
    `menu_code`     VARCHAR(64)   NOT NULL                COMMENT '菜单编码（路由 name，前端必填）',
    `menu_name`     VARCHAR(64)   NOT NULL                COMMENT '菜单名称（路由 title）',
    `url`           VARCHAR(256)                           COMMENT '菜单URL（路由 path），如 /system/user',
    `parent_id`     BIGINT        NOT NULL DEFAULT 0      COMMENT '父菜单ID，顶级菜单为0',
    `menu_type`     VARCHAR(4)    NOT NULL DEFAULT '1'    COMMENT '菜单类型：0-目录，1-菜单，2-按钮',
    `icon`          VARCHAR(128)                           COMMENT '图标，如 ant-design:setting-outlined',
    `sort`          INT           NOT NULL DEFAULT 0       COMMENT '排序号（升序）',
    `permission`    VARCHAR(128)                           COMMENT '权限标识',
    `visible`       TINYINT       NOT NULL DEFAULT 1      COMMENT '是否可见：1-是，0-否',
    `remark`        VARCHAR(512)                           COMMENT '备注',
    `create_id`     BIGINT                                 COMMENT '创建人ID',
    `create_name`   VARCHAR(64)                            COMMENT '创建人名称',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`     BIGINT                                 COMMENT '更新人ID',
    `update_name`   VARCHAR(64)                            COMMENT '更新人名称',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';
