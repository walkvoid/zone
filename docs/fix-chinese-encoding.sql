-- ============================================
-- 修复菜单/用户/角色表中文乱码（错误编码写入的数据）
-- 执行方式：
-- docker exec -i mysql8 mysql -uroot -proot123456 --default-character-set=utf8mb4 zone < docs/fix-chinese-encoding.sql
-- ============================================

SET NAMES utf8mb4;

-- 用户表
UPDATE `user_info`
SET `nickname` = '管理员'
WHERE `username` = 'admin';

-- 角色表
UPDATE `role`
SET `role_name` = '超级管理员',
    `description` = '系统最高权限角色'
WHERE `role_code` = 'ROLE_ADMIN';

UPDATE `role`
SET `role_name` = '普通用户',
    `description` = '基础用户角色'
WHERE `role_code` = 'ROLE_USER';

-- 菜单表（金融模块乱码项）
UPDATE `menu` SET `menu_name` = '金融管理' WHERE `menu_code` = 'FinanceManage';
UPDATE `menu` SET `menu_name` = '股票指标' WHERE `menu_code` = 'StockIndicator';
UPDATE `menu` SET `menu_name` = '基金指标' WHERE `menu_code` = 'FundIndicator';
UPDATE `menu` SET `menu_name` = '宏观指标' WHERE `menu_code` = 'MacroIndicator';
