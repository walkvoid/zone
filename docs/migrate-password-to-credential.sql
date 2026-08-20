-- ============================================
-- 阶段 2：将 user_info.password 迁移到 user_credential
-- 执行顺序：init-auth-tables.sql → 本脚本
-- ============================================

-- 1. 用户名登录身份
INSERT INTO `user_identity` (`user_id`, `identity_type`, `identifier`, `verified`, `create_time`, `update_time`)
SELECT u.id, 'USERNAME', u.username, 1, u.create_time, u.update_time
FROM `user_info` u
WHERE u.username IS NOT NULL AND u.username <> ''
ON DUPLICATE KEY UPDATE `update_time` = VALUES(`update_time`);

-- 2. 密码凭证
INSERT INTO `user_credential` (`user_id`, `credential_type`, `secret_hash`, `create_time`, `update_time`)
SELECT u.id, 'PASSWORD', u.password, u.create_time, u.update_time
FROM `user_info` u
WHERE u.password IS NOT NULL AND u.password <> '' AND u.password <> 'CHANGE_ME_TO_BCRYPT'
ON DUPLICATE KEY UPDATE
    `secret_hash` = VALUES(`secret_hash`),
    `update_time` = VALUES(`update_time`);

-- 3. 可选：手机/邮箱身份（未验证）
INSERT INTO `user_identity` (`user_id`, `identity_type`, `identifier`, `verified`, `create_time`, `update_time`)
SELECT u.id, 'PHONE', u.phone, 0, u.create_time, u.update_time
FROM `user_info` u
WHERE u.phone IS NOT NULL AND u.phone <> ''
ON DUPLICATE KEY UPDATE `update_time` = VALUES(`update_time`);

INSERT INTO `user_identity` (`user_id`, `identity_type`, `identifier`, `verified`, `create_time`, `update_time`)
SELECT u.id, 'EMAIL', u.email, 0, u.create_time, u.update_time
FROM `user_info` u
WHERE u.email IS NOT NULL AND u.email <> ''
ON DUPLICATE KEY UPDATE `update_time` = VALUES(`update_time`);

-- 4. user_info 废弃 password 列
ALTER TABLE `user_info` MODIFY COLUMN `password` VARCHAR(128) NULL COMMENT '已废弃，请使用 user_credential';

-- 确认无误后可执行：
-- ALTER TABLE `user_info` DROP COLUMN `password`;
