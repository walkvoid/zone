package com.github.walkvoid.zone.ai.prompt;

import com.github.walkvoid.wvframework.fileservice.FileService;
import com.github.walkvoid.wvframework.fileservice.entity.FileInfo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 从 MinIO 文件抽取纯文本（docx 走 POI，文本类直接读）。
 */
@Component
public class DocumentTextExtractor {

    private final FileService fileService;

    public DocumentTextExtractor(FileService fileService) {
        this.fileService = fileService;
    }

    public String extract(Long fileId) {
        FileInfo info = fileService.getById(fileId);
        if (info == null) {
            throw new IllegalArgumentException("文件不存在: " + fileId);
        }
        String ext = info.getFileExt() == null ? "" : info.getFileExt().toLowerCase(Locale.ROOT);
        if ("docx".equals(ext)) {
            return extractDocx(fileId);
        }
        // 文本类：上限 2MB 字符，供后续切割/注入
        return fileService.readAsText(fileId, 2_000_000);
    }

    private String extractDocx(Long fileId) {
        try (InputStream in = fileService.download(fileId);
             XWPFDocument doc = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder();
            for (IBodyElement element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    String text = paragraph.getText();
                    if (StringUtils.hasText(text)) {
                        sb.append(text.trim()).append('\n');
                    }
                } else if (element instanceof XWPFTable table) {
                    sb.append(tableToMarkdown(table)).append('\n');
                }
            }
            String result = sb.toString().trim();
            if (!StringUtils.hasText(result)) {
                throw new IllegalArgumentException("DOCX 未解析到可用文本: " + fileId);
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DOCX 解析失败: " + e.getMessage(), e);
        }
    }

    private static String tableToMarkdown(XWPFTable table) {
        StringBuilder sb = new StringBuilder();
        boolean headerDone = false;
        for (XWPFTableRow row : table.getRows()) {
            String line = row.getTableCells().stream()
                    .map(XWPFTableCell::getText)
                    .map(t -> t == null ? "" : t.replace('|', '/').trim())
                    .collect(Collectors.joining(" | "));
            sb.append("| ").append(line).append(" |\n");
            if (!headerDone) {
                int cols = Math.max(1, row.getTableCells().size());
                sb.append("| ");
                for (int i = 0; i < cols; i++) {
                    if (i > 0) {
                        sb.append(" | ");
                    }
                    sb.append("---");
                }
                sb.append(" |\n");
                headerDone = true;
            }
        }
        return sb.toString();
    }

    /** 判断扩展名是否需要走向量材料管线的候选（docx 或超长文本） */
    public static boolean isDocx(FileInfo info) {
        if (info == null || info.getFileExt() == null) {
            return false;
        }
        return "docx".equalsIgnoreCase(info.getFileExt());
    }
}
