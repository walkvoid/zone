-- file_ids 替换 file_id；file_info 增加 expire_time（7 天过期）
-- 库：zone_ai

-- 1) run_record：去掉 file_id，改为 file_ids JSON
ALTER TABLE `prompt_template_run_record`
    ADD COLUMN `file_ids` TEXT DEFAULT NULL COMMENT '关联文档 file_info.id 列表 JSON' AFTER `run_result`;

-- 若已有 file_id 可先迁移再删（按需执行）
-- UPDATE prompt_template_run_record SET file_ids = CONCAT('[', file_id, ']') WHERE file_id IS NOT NULL AND (file_ids IS NULL OR file_ids = '');
ALTER TABLE `prompt_template_run_record` DROP COLUMN `file_id`;

-- 2) file_info 过期时间
ALTER TABLE `file_info`
    ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间' AFTER `access_url`;

ALTER TABLE `file_info`
    ADD KEY `idx_expire_time` (`expire_time`);

-- 已有数据默认 7 天后过期（从 create_time 起算）
UPDATE `file_info`
SET `expire_time` = DATE_ADD(`create_time`, INTERVAL 7 DAY)
WHERE `expire_time` IS NULL AND `create_time` IS NOT NULL;
