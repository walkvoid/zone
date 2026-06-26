package com.github.walkvoid.zone.ai.api;

import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Prompt模板 API — 供其他模块跨模块调用
 *
 * @author walkvoid
 */
public interface PromptTemplateApi {

    @GetMapping("/prompt-template/code/{templateCode}")
    PromptTemplateDTO getByCode(@PathVariable String templateCode);

    /**
     * 执行 prompt 模板，调用大模型并返回响应文本
     *
     * @param templateCode 模板编码
     * @param variables    变量替换 Map（key → value）
     * @return 大模型响应文本
     */
    String executePrompt(String templateCode, java.util.Map<String, String> variables);
}
