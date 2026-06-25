package com.github.walkvoid.zone.ai.api;

import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Prompt模板 API（Feign，供其他模块调用）
 *
 * @author walkvoid
 */
public interface PromptTemplateApi {

    @GetMapping("/prompt-template/code/{templateCode}")
    PromptTemplateDTO getByCode(@PathVariable String templateCode);
}
