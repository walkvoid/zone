-- 演示用 mock：一条功能点 + 两个 patch。可重复执行。
-- mysql -u... -p... zone_ai < zone-ai/docs/mock-ai-code-change.sql

USE `zone_ai`;

INSERT INTO `ai_code_change` (
    `conversation_id`, `turn_no`, `message_id`, `bot_id`, `bot_code`,
    `chat_id`, `user_id`, `channel_type`, `title`, `request_text`,
    `write_mode`, `status`, `patch_count`, `create_time`, `update_time`
) SELECT
    'weixin:aib_demo:wr_group_demo',
    'mock-turn-demo-001',
    'msg_demo_001',
    'aib_demo',
    'supply-chain',
    'wr_group_demo',
    'zhangsan',
    'WEIXIN',
    '把放款成功文案改清楚，并补一行日志',
    '帮我把 PayListener 里「放款成功写回」改成更清楚的注释，另外在 PromptTemplateApi.executePrompt 开头加一行 info 日志。',
    'DIFF_FILE',
    0,
    2,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `ai_code_change` WHERE `turn_no` = 'mock-turn-demo-001'
);

SET @change_id := (SELECT `id` FROM `ai_code_change` WHERE `turn_no` = 'mock-turn-demo-001');

INSERT INTO `ai_code_change_patch` (
    `change_id`, `source_path`, `patch_file`, `tool_name`, `new_file`,
    `added_lines`, `removed_lines`, `unified_diff`, `base_content`, `new_content`,
    `status`, `create_time`
) SELECT
    @change_id,
    'zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/PayListener.java',
    'zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/fix_20260818_1400_PayListener.patch',
    'applyReplace',
    0,
    1,
    1,
    'diff --git a/zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/PayListener.java b/zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/PayListener.java\n--- a/zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/PayListener.java\n+++ b/zone-finance/src/main/java/com/github/walkvoid/zone/finance/listener/PayListener.java\n@@ -12,7 +12,7 @@ public class PayListener {\n     public void onSuccess(PayEvent event) {\n-        // 放款成功写回\n+        // 放款成功后写回融资单状态，避免重复回调\n         statusService.markSuccess(event.getAssetId());\n     }\n }\n',
    'package com.github.walkvoid.zone.finance.listener;\n\npublic class PayListener {\n    private final StatusService statusService;\n\n    public PayListener(StatusService statusService) {\n        this.statusService = statusService;\n    }\n\n    public void onSuccess(PayEvent event) {\n        // 放款成功写回\n        statusService.markSuccess(event.getAssetId());\n    }\n}\n',
    'package com.github.walkvoid.zone.finance.listener;\n\npublic class PayListener {\n    private final StatusService statusService;\n\n    public PayListener(StatusService statusService) {\n        this.statusService = statusService;\n    }\n\n    public void onSuccess(PayEvent event) {\n        // 放款成功后写回融资单状态，避免重复回调\n        statusService.markSuccess(event.getAssetId());\n    }\n}\n',
    0,
    NOW()
WHERE @change_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `ai_code_change_patch`
      WHERE `change_id` = @change_id
        AND `source_path` LIKE '%PayListener.java'
  );

INSERT INTO `ai_code_change_patch` (
    `change_id`, `source_path`, `patch_file`, `tool_name`, `new_file`,
    `added_lines`, `removed_lines`, `unified_diff`, `base_content`, `new_content`,
    `status`, `create_time`
) SELECT
    @change_id,
    'zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/PromptTemplateApi.java',
    'zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/fix_20260818_1401_PromptTemplateApi.patch',
    'applyPatch',
    0,
    1,
    0,
    'diff --git a/zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/PromptTemplateApi.java b/zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/PromptTemplateApi.java\n--- a/zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/PromptTemplateApi.java\n+++ b/zone-ai/zone-ai-business/src/main/java/com/github/walkvoid/zone/ai/business/service/PromptTemplateApi.java\n@@ -20,6 +20,7 @@ public class PromptTemplateApi {\n\n     public String executePrompt(String templateCode, Map<String, String> variables) {\n+        log.info("executePrompt templateCode={}", templateCode);\n         PromptTemplate template = dao.selectByCode(templateCode);\n         if (template == null) {\n             throw new IllegalArgumentException("template not found: " + templateCode);\n',
    'package com.github.walkvoid.zone.ai.business.service;\n\npublic class PromptTemplateApi {\n    private static final Logger log = LoggerFactory.getLogger(PromptTemplateApi.class);\n\n    public String executePrompt(String templateCode, Map<String, String> variables) {\n        PromptTemplate template = dao.selectByCode(templateCode);\n        if (template == null) {\n            throw new IllegalArgumentException("template not found: " + templateCode);\n        }\n        return render(template, variables);\n    }\n}\n',
    'package com.github.walkvoid.zone.ai.business.service;\n\npublic class PromptTemplateApi {\n    private static final Logger log = LoggerFactory.getLogger(PromptTemplateApi.class);\n\n    public String executePrompt(String templateCode, Map<String, String> variables) {\n        log.info("executePrompt templateCode={}", templateCode);\n        PromptTemplate template = dao.selectByCode(templateCode);\n        if (template == null) {\n            throw new IllegalArgumentException("template not found: " + templateCode);\n        }\n        return render(template, variables);\n    }\n}\n',
    0,
    NOW()
WHERE @change_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `ai_code_change_patch`
      WHERE `change_id` = @change_id
        AND `source_path` LIKE '%PromptTemplateApi.java'
  );

UPDATE `ai_code_change`
SET `patch_count` = (
    SELECT COUNT(*) FROM `ai_code_change_patch` WHERE `change_id` = @change_id
)
WHERE `id` = @change_id;
