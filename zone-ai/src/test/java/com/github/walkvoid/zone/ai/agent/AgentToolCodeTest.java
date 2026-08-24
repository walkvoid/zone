package com.github.walkvoid.zone.ai.agent;

import com.github.walkvoid.zone.ai.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.tool.DbInsertTool;
import com.github.walkvoid.zone.ai.tool.FileUploadTool;
import com.github.walkvoid.zone.ai.tool.KnowledgeSearchTool;
import com.github.walkvoid.zone.ai.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.tool.SqlQueryTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AgentToolCodeTest {

    @Test
    void parseDefaultWhenBlank() {
        assertEquals(List.of(AgentToolCode.LOG, AgentToolCode.SQL, AgentToolCode.REPO_READ, AgentToolCode.KNOWLEDGE),
                AgentToolCode.parse(""));
        assertEquals(List.of(AgentToolCode.LOG, AgentToolCode.SQL, AgentToolCode.REPO_READ, AgentToolCode.KNOWLEDGE),
                AgentToolCode.parse(null));
    }

    @Test
    void parseCommaSeparatedAndAliases() {
        assertEquals(List.of(AgentToolCode.SQL, AgentToolCode.REPO_CHANGE),
                AgentToolCode.parse("sql, repo-change"));
        assertEquals(List.of(AgentToolCode.LOG), AgentToolCode.parse("log,unknown,LOG"));
        assertEquals(List.of(AgentToolCode.LOG, AgentToolCode.DB_INSERT, AgentToolCode.FILE_UPLOAD),
                AgentToolCode.parse("log,db_insert,file_upload"));
    }

    @Test
    void insertAndUploadToolsReady() {
        assertTrue(AgentToolCode.DB_INSERT.ready());
        assertTrue(AgentToolCode.FILE_UPLOAD.ready());
        assertTrue(AgentToolCode.SQL.ready());
    }

    @Test
    void registryResolvesBeansInOrder() {
        AppLogSearchTool log = mock(AppLogSearchTool.class);
        SqlQueryTool sql = mock(SqlQueryTool.class);
        RepoReadTool read = mock(RepoReadTool.class);
        RepoChangeTool change = mock(RepoChangeTool.class);
        KnowledgeSearchTool knowledge = mock(KnowledgeSearchTool.class);
        DbInsertTool dbInsert = mock(DbInsertTool.class);
        FileUploadTool fileUpload = mock(FileUploadTool.class);
        AgentToolRegistry registry = new AgentToolRegistry(
                log, sql, read, change, knowledge, dbInsert, fileUpload);
        Object[] tools = registry.resolve(List.of(
                AgentToolCode.SQL, AgentToolCode.LOG, AgentToolCode.KNOWLEDGE,
                AgentToolCode.DB_INSERT, AgentToolCode.FILE_UPLOAD));
        assertEquals(5, tools.length);
        assertSame(sql, tools[0]);
        assertSame(log, tools[1]);
        assertSame(knowledge, tools[2]);
        assertSame(dbInsert, tools[3]);
        assertSame(fileUpload, tools[4]);
        assertTrue(registry.isRegistered(AgentToolCode.DB_INSERT));
        assertTrue(registry.isRegistered(AgentToolCode.FILE_UPLOAD));
    }
}
