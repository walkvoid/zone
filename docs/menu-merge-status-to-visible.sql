-- 菜单 status 字段合并到 visible
-- 执行：docker exec -i mysql8 mysql -uroot -proot123456 zone < docs/menu-merge-status-to-visible.sql

-- 原 status=0（禁用）的菜单统一设为不可见
UPDATE `menu` SET `visible` = 0 WHERE `status` = 0;

-- 删除 status 字段（索引可能不存在，忽略即可）
ALTER TABLE `menu` DROP COLUMN `status`;
