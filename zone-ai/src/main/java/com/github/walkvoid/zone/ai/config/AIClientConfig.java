//package com.github.walkvoid.zone.ai.config;
//
//import com.github.walkvoid.zone.ai.tool.AppLogSearchTool;
//import com.github.walkvoid.zone.ai.tool.RepoChangeTool;
//import com.github.walkvoid.zone.ai.tool.RepoReadTool;
//import com.github.walkvoid.zone.ai.tool.SqlQueryTool;
//import org.springframework.ai.tool.ToolCallbackProvider;
//import org.springframework.ai.tool.method.MethodToolCallbackProvider;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Spring AI / MCP 相关装配。
// * MethodToolCallbackProvider 会扫描 @Tool 方法，
// * 由 spring-ai-starter-mcp-server-webmvc 自动发布为 MCP tools。
// */
//@Configuration
//public class AIClientConfig {
//
//    @Bean
//    public ToolCallbackProvider mcpToolCallbackProvider(
//            AppLogSearchTool appLogSearchTool,
//            SqlQueryTool sqlQueryTool,
//            RepoReadTool repoReadTool,
//            RepoChangeTool repoChangeTool) {
//        return MethodToolCallbackProvider.builder()
//                .toolObjects(appLogSearchTool, sqlQueryTool, repoReadTool, repoChangeTool)
//                .build();
//    }
//}
