package com.github.walkvoid.zone.ai.tool.sql;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGuardTest {

    private static final Set<String> ALLOWED = Set.of("financing_order", "financing_flow");

    @Test
    void allowsSelectAndAppendsLimit() {
        String sql = SqlGuard.guardSelect(
                "SELECT id, status FROM financing_order WHERE financing_no = 'F1'",
                ALLOWED, 50);
        assertTrue(sql.endsWith(" LIMIT 50"));
    }

    @Test
    void capsExistingLimit() {
        String sql = SqlGuard.guardSelect(
                "SELECT * FROM financing_order LIMIT 10000",
                ALLOWED, 50);
        assertEquals("SELECT * FROM financing_order LIMIT 50", sql);
    }

    @Test
    void allowsJoinOfWhitelistedTables() {
        String sql = SqlGuard.guardSelect(
                "SELECT o.id FROM financing_order o JOIN financing_flow f ON o.financing_no = f.financing_no",
                ALLOWED, 20);
        assertTrue(sql.toUpperCase().contains("LIMIT 20"));
    }

    @Test
    void allowsCommaJoin() {
        SqlGuard.guardSelect(
                "SELECT * FROM financing_order, financing_flow WHERE financing_order.id = financing_flow.id",
                ALLOWED, 10);
    }

    @Test
    void rejectsUnknownTable() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("SELECT * FROM mysql_user", ALLOWED, 10));
    }

    @Test
    void rejectsSystemSchema() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("SELECT * FROM mysql.user", ALLOWED, 10));
    }

    @Test
    void rejectsWriteKeyword() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("DELETE FROM financing_order", ALLOWED, 10));
    }

    @Test
    void rejectsMultipleStatements() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("SELECT * FROM financing_order; DROP TABLE financing_order", ALLOWED, 10));
    }

    @Test
    void rejectsComments() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("SELECT * FROM financing_order -- x", ALLOWED, 10));
    }

    @Test
    void rejectsSelectWithoutTable() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.guardSelect("SELECT USER()", ALLOWED, 10));
    }

    @Test
    void allowsSchemaQualifiedTable() {
        Set<String> allowed = Set.of("jinkoscf_tools.user_info");
        String sql = SqlGuard.guardSelect(
                "SELECT * FROM jinkoscf_tools.user_info WHERE id = 1",
                allowed, 10);
        assertTrue(sql.endsWith(" LIMIT 10"));
        assertEquals("jinkoscf_tools.user_info",
                SqlGuard.requireAllowedTable("user_info", allowed));
    }

    @Test
    void requireIdentRejectsInjection() {
        assertThrows(IllegalArgumentException.class, () ->
                SqlGuard.requireIdent("id;drop", "column"));
    }
}
