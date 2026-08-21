package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.fileservice.FileService;
import com.github.walkvoid.wvframework.fileservice.entity.FileInfo;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 走网关 /ai/** 的文件接口，复用 wvframework-fileservice。
 */
@Tag(name = "AI文件")
@RestController
@RequestMapping("/ai/file")
public class AiFileController {

    public static final String BIZ_PROMPT_RUN = "prompt_run";

    @Autowired
    private FileService fileService;

    @Operation(summary = "上传文件（Prompt 运行附件默认 bizCode=prompt_run）")
    @PostMapping("/upload")
    public ApiResult<FileInfo> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizCode", required = false, defaultValue = BIZ_PROMPT_RUN) String bizCode) {
        return ApiResult.ok(fileService.upload(file, bizCode), "上传成功");
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        FileInfo info = fileService.getById(id);
        if (info == null) {
            response.setStatus(404);
            return;
        }
        try (InputStream is = fileService.download(id);
             OutputStream os = response.getOutputStream()) {
            response.setContentType(info.getContentType() != null
                    ? info.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String filename = URLEncoder.encode(info.getOriginalName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString());
            is.transferTo(os);
            os.flush();
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    @Operation(summary = "文件详情")
    @GetMapping("/{id}")
    public ApiResult<FileInfo> getById(@PathVariable("id") Long id) {
        FileInfo info = fileService.getById(id);
        if (info == null) {
            return ApiResult.error(404, "文件不存在");
        }
        return ApiResult.ok(info);
    }

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public ApiResult<PageDTO<FileInfo>> page(PageRequest<Void> pageRequest) {
        return ApiResult.ok(fileService.page(pageRequest));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        fileService.delete(id);
        return ApiResult.ok("OK", "删除成功");
    }

    @Operation(summary = "临时访问链接")
    @GetMapping("/access-url/{id}")
    public ApiResult<String> getAccessUrl(@PathVariable("id") Long id,
                                          @RequestParam(defaultValue = "3600") long expirySeconds) {
        return ApiResult.ok(fileService.getAccessUrl(id, Duration.ofSeconds(expirySeconds)));
    }
}
