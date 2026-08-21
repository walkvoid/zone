package com.github.walkvoid.zone.ai.agent;

import com.github.walkvoid.zone.ai.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.tool.SqlQueryTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AgentToolCodeTest {

    @Test
    void parseDefaultWhenBlank() {
        assertEquals(List.of(AgentToolCode.LOG, AgentToolCode.SQL, AgentToolCode.REPO_READ),
                AgentToolCode.parse(""));
        assertEquals(List.of(AgentToolCode.LOG, AgentToolCode.SQL, AgentToolCode.REPO_READ),
                AgentToolCode.parse(null));
    }

    @Test
    void parseCommaSeparatedAndAliases() {
        assertEquals(List.of(AgentToolCode.SQL, AgentToolCode.REPO_CHANGE),
                AgentToolCode.parse("sql, repo-change"));
        assertEquals(List.of(AgentToolCode.LOG), AgentToolCode.parse("log,unknown,LOG"));
    }

    @Test
    void registryResolvesBeansInOrder() {
        AppLogSearchTool log = mock(AppLogSearchTool.class);
        SqlQueryTool sql = mock(SqlQueryTool.class);
        RepoReadTool read = mock(RepoReadTool.class);
        RepoChangeTool change = mock(RepoChangeTool.class);
        AgentToolRegistry registry = new AgentToolRegistry(log, sql, read, change);
        Object[] tools = registry.resolve(List.of(AgentToolCode.SQL, AgentToolCode.LOG));
        assertEquals(2, tools.length);
        assertSame(sql, tools[0]);
        assertSame(log, tools[1]);
    }
}
