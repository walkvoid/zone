-- ============================================
-- Zone AI 管理菜单初始化
-- 与 zone-ai-business 演示页能力对齐
-- ============================================

-- 顶级目录：AI 管理
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, create_id, update_id, create_time, update_time) VALUES
(600, 0, 'AIManage', 'AI管理', '/ai', '0', 'ant-design:robot-outlined', 50, NULL, 1, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    url = VALUES(url),
    menu_type = VALUES(menu_type),
    icon = VALUES(icon),
    sort = VALUES(sort),
    visible = VALUES(visible),
    update_id = VALUES(update_id),
    update_time = VALUES(update_time);

-- 子菜单
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, create_id, update_id, create_time, update_time) VALUES
(601, 600, 'AIPromptTemplate', '模板配置', '/ai/prompt-template', '1', 'ant-design:file-text-outlined', 1, 'AI:PromptTemplate:List', 1, 1, 1, NOW(), NOW()),
(602, 600, 'AIModel', '模型配置', '/ai/ai-model', '1', 'ant-design:api-outlined', 2, 'AI:Model:List', 1, 1, 1, NOW(), NOW()),
(603, 600, 'AIBotConfig', '机器人配置', '/ai/ai-bot-config', '1', 'ant-design:android-outlined', 3, 'AI:BotConfig:List', 1, 1, 1, NOW(), NOW()),
(604, 600, 'AIAgentTurn', '对话日志', '/ai/ai-agent-turn', '1', 'ant-design:history-outlined', 4, 'AI:AgentTurn:List', 1, 1, 1, NOW(), NOW()),
(605, 600, 'AICodeChange', '改代码历史', '/ai/ai-code-change', '1', 'ant-design:code-outlined', 5, 'AI:CodeChange:List', 1, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    url = VALUES(url),
    menu_type = VALUES(menu_type),
    icon = VALUES(icon),
    sort = VALUES(sort),
    permission = VALUES(permission),
    visible = VALUES(visible),
    update_id = VALUES(update_id),
    update_time = VALUES(update_time);
