-- ============================================
-- Zone Admin 菜单初始化数据
-- ============================================

-- 清理旧数据
DELETE FROM menu WHERE id > 0;

-- 仪表盘（顶级目录）
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(100, 0, 'Dashboard', '仪表盘', '/dashboard', '0', 'ant-design:dashboard-outlined', 1, NULL, 1, 1, 1, 'admin', NOW(), NOW());

INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(101, 100, 'Analytics', '分析页', '/analytics', '1', 'ant-design:bar-chart-outlined', 1, 'Dashboard:Analytics', 1, 1, 1, 'admin', NOW(), NOW());

INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(102, 100, 'Workspace', '工作台', '/workspace', '1', 'ant-design:appstore-outlined', 2, 'Dashboard:Workspace', 1, 1, 1, 'admin', NOW(), NOW());

-- ============================================
-- 系统管理（顶级目录）—— 3个子菜单
-- ============================================
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(200, 0, 'System', '系统管理', '/system', '0', 'ant-design:setting-outlined', 99, NULL, 1, 1, 1, 'admin', NOW(), NOW());

INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(201, 200, 'SystemMenu', '菜单管理', '/system/menu', '1', 'ant-design:menu-outlined', 1, 'System:Menu:List', 1, 1, 1, 'admin', NOW(), NOW());

INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(202, 200, 'SystemRole', '角色管理', '/system/role', '1', 'ant-design:team-outlined', 2, 'System:Role:List', 1, 1, 1, 'admin', NOW(), NOW());

INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(203, 200, 'SystemUser', '用户管理', '/system/user', '1', 'ant-design:user-outlined', 3, 'System:User:List', 1, 1, 1, 'admin', NOW(), NOW());

-- ============================================
-- 按钮权限
-- ============================================
-- 菜单管理按钮
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(20101, 201, 'SystemMenuCreate', '新增', '', '2', NULL, 1, 'System:Menu:Create', 1, 1, 1, 'admin', NOW(), NOW()),
(20102, 201, 'SystemMenuEdit', '编辑', '', '2', NULL, 2, 'System:Menu:Edit', 1, 1, 1, 'admin', NOW(), NOW()),
(20103, 201, 'SystemMenuDelete', '删除', '', '2', NULL, 3, 'System:Menu:Delete', 1, 1, 1, 'admin', NOW(), NOW());

-- 角色管理按钮
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(20201, 202, 'SystemRoleCreate', '新增', '', '2', NULL, 1, 'System:Role:Create', 1, 1, 1, 'admin', NOW(), NOW()),
(20202, 202, 'SystemRoleEdit', '编辑', '', '2', NULL, 2, 'System:Role:Edit', 1, 1, 1, 'admin', NOW(), NOW()),
(20203, 202, 'SystemRoleDelete', '删除', '', '2', NULL, 3, 'System:Role:Delete', 1, 1, 1, 'admin', NOW(), NOW());

-- 用户管理按钮
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(20301, 203, 'SystemUserCreate', '新增', '', '2', NULL, 1, 'System:User:Create', 1, 1, 1, 'admin', NOW(), NOW()),
(20302, 203, 'SystemUserEdit', '编辑', '', '2', NULL, 2, 'System:User:Edit', 1, 1, 1, 'admin', NOW(), NOW()),
(20303, 203, 'SystemUserDelete', '删除', '', '2', NULL, 3, 'System:User:Delete', 1, 1, 1, 'admin', NOW(), NOW());

-- 关于
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, create_name, create_time, update_time) VALUES
(400, 0, 'About', '关于', '/about', '1', 'ant-design:copyright-outlined', 999, NULL, 1, 1, 1, 'admin', NOW(), NOW());
