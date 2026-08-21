package com.github.walkvoid.zone.ai.agent;

import com.github.walkvoid.zone.ai.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.tool.KnowledgeSearchTool;
import com.github.walkvoid.zone.ai.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.tool.SqlQueryTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 把 {@link AgentToolCode} 解析成 ChatClient 可挂载的 Tool Bean。
 */
@Component
public class AgentToolRegistry {

    private final Map<AgentToolCode, Object> tools = new EnumMap<>(AgentToolCode.class);

    public AgentToolRegistry(AppLogSearchTool appLogSearchTool,
                             SqlQueryTool sqlQueryTool,
                             RepoReadTool repoReadTool,
                             RepoChangeTool repoChangeTool,
                             KnowledgeSearchTool knowledgeSearchTool) {
        tools.put(AgentToolCode.LOG, appLogSearchTool);
        tools.put(AgentToolCode.SQL, sqlQueryTool);
        tools.put(AgentToolCode.REPO_READ, repoReadTool);
        tools.put(AgentToolCode.REPO_CHANGE, repoChangeTool);
        tools.put(AgentToolCode.KNOWLEDGE, knowledgeSearchTool);
    }

    public Object[] resolve(List<AgentToolCode> codes) {
        List<Object> selected = new ArrayList<>();
        if (codes == null) {
            return new Object[0];
        }
        for (AgentToolCode code : codes) {
            Object tool = tools.get(code);
            if (tool != null) {
                selected.add(tool);
            }
        }
        return selected.toArray();
    }
}
