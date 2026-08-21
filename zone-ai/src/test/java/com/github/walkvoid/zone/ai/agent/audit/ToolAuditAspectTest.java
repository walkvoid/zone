package com.github.walkvoid.zone.ai.agent.audit;

import com.github.walkvoid.zone.ai.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.tool.SqlQueryTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ToolAuditAspectTest {

    @Test
    void toolCodeOfMapsBeanNames() {
        assertEquals("log", ToolAuditAspect.toolCodeOf(mock(AppLogSearchTool.class)));
        assertEquals("sql", ToolAuditAspect.toolCodeOf(mock(SqlQueryTool.class)));
        assertEquals("repo_read", ToolAuditAspect.toolCodeOf(mock(RepoReadTool.class)));
        assertEquals("repo_change", ToolAuditAspect.toolCodeOf(mock(RepoChangeTool.class)));
        assertEquals("unknown", ToolAuditAspect.toolCodeOf(new Object()));
        assertEquals("unknown", ToolAuditAspect.toolCodeOf(null));
    }

    @Test
    void toolCodeOfStripsCglibSuffix() {
        class AppLogSearchTool$$SpringCGLIB$$0 {
        }
        assertEquals("log", ToolAuditAspect.toolCodeOf(new AppLogSearchTool$$SpringCGLIB$$0()));
    }
}
