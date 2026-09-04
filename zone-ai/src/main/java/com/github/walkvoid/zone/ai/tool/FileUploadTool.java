package com.github.walkvoid.zone.ai.tool;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.fileservice.FileService;
import com.github.walkvoid.wvframework.fileservice.entity.FileInfo;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 将文本 / Base64 内容上传到 MinIO（wvframework-fileservice）。
 */
@Component
public class FileUploadTool {

    private static final Logger log = LoggerFactory.getLogger(FileUploadTool.class);
    private static final int MAX_TEXT_CHARS = 2_000_000;
    private static final int MAX_BYTES = 20 * 1024 * 1024;

    private final FileService fileService;

    public FileUploadTool(FileService fileService) {
        this.fileService = fileService;
    }

    @Tool(description = "把文本内容上传到 MinIO 文件服务并返回 fileId。"
            + "适用于 Prompt 运行结果落盘、导出 md/txt/json/csv。"
            + "bizCode 建议用 prompt_result；默认文件名 result.md。")
    public JsonNode uploadText(
            @ToolParam(description = "要上传的文本正文", required = true) String content,
            @ToolParam(description = "原始文件名，如 summary.md / data.json；缺省 result.md") String fileName,
            @ToolParam(description = "业务编码，决定对象路径前缀；缺省 prompt_result") String bizCode) {
        try {
            if (!StringUtils.hasText(content)) {
                return error("content is required");
            }
            if (content.length() > MAX_TEXT_CHARS) {
                return error("content too large, maxChars=" + MAX_TEXT_CHARS);
            }
            String name = StringUtils.hasText(fileName) ? fileName.trim() : "result.md";
            String biz = StringUtils.hasText(bizCode) ? bizCode.trim() : "prompt_result";
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            FileInfo info = fileService.uploadBytes(bytes, name, null, biz);
            return ok(info);
        } catch (Exception e) {
            return fail("uploadText", e);
        }
    }

    @Tool(description = "把 Base64 编码的二进制内容上传到 MinIO。"
            + "fileName 必填且需带扩展名；contentType 可选。单文件上限约 20MB。")
    public JsonNode uploadBase64(
            @ToolParam(description = "Base64 内容（可带 data:...;base64, 前缀）", required = true) String base64,
            @ToolParam(description = "原始文件名，如 report.docx", required = true) String fileName,
            @ToolParam(description = "MIME，如 application/pdf；可空") String contentType,
            @ToolParam(description = "业务编码；缺省 prompt_result") String bizCode) {
        try {
            if (!StringUtils.hasText(base64)) {
                return error("base64 is required");
            }
            if (!StringUtils.hasText(fileName)) {
                return error("fileName is required");
            }
            String raw = base64.trim();
            int comma = raw.indexOf(',');
            if (raw.startsWith("data:") && comma > 0) {
                raw = raw.substring(comma + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(raw);
            if (bytes.length == 0) {
                return error("decoded content is empty");
            }
            if (bytes.length > MAX_BYTES) {
                return error("file too large, maxBytes=" + MAX_BYTES);
            }
            String biz = StringUtils.hasText(bizCode) ? bizCode.trim() : "prompt_result";
            FileInfo info = fileService.uploadBytes(bytes, fileName.trim(), contentType, biz);
            return ok(info);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return fail("uploadBase64", e);
        }
    }

    private ObjectNode ok(FileInfo info) {
        ObjectNode node = JsonUtils.getObjectMapper().createObjectNode();
        node.put("success", true);
        if (info.getId() != null) {
            node.put("fileId", info.getId());
        }
        node.put("fileKey", info.getFileKey());
        node.put("originalName", info.getOriginalName());
        node.put("objectName", info.getObjectName());
        node.put("bizCode", info.getBizCode());
        node.put("contentType", info.getContentType());
        node.put("fileSize", info.getFileSize() == null ? 0L : info.getFileSize());
        if (info.getExpireTime() != null) {
            node.put("expireTime", info.getExpireTime().toString());
        }
        log.info("FileUploadTool ok fileId={}, name={}, size={}",
                info.getId(), info.getOriginalName(), info.getFileSize());
        return node;
    }

    private ObjectNode error(String message) {
        ObjectNode node = JsonUtils.getObjectMapper().createObjectNode();
        node.put("success", false);
        node.put("message", message);
        return node;
    }

    private ObjectNode fail(String action, Exception e) {
        log.warn("FileUploadTool {} failed: {}", action, e.getMessage());
        return error(action + " failed: " + e.getMessage());
    }
}
