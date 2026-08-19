package com.github.walkvoid.zone.ai.business;

import com.fasterxml.jackson.databind.node.NullNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonNodeUtilsTest {

    @Test
    void isAbsentCoversNullMissingAndJsonNull() throws Exception {
        assertTrue(JsonNodeUtils.isAbsent(null));
        assertTrue(JsonNodeUtils.isAbsent(NullNode.getInstance()));
        assertTrue(JsonNodeUtils.isAbsent(JsonNodeUtils.path(JsonUtils.getObjectMapper().readTree("{}"), "missing")));
        assertFalse(JsonNodeUtils.isAbsent(JsonUtils.getObjectMapper().readTree("{\"a\":1}").get("a")));
    }

    @Test
    void pathAndHasWalkNestedFields() throws Exception {
        var root = JsonUtils.getObjectMapper().readTree("{\"headers\":{\"req_id\":\"abc\"},\"cmd\":\"ping\"}");
        assertEquals("abc", JsonNodeUtils.path(root, "headers", "req_id").asText());
        assertTrue(JsonNodeUtils.has(root, "cmd"));
        assertTrue(JsonNodeUtils.has(root, "headers", "req_id"));
        assertFalse(JsonNodeUtils.has(root, "missing"));
        assertFalse(JsonNodeUtils.has(null, "cmd"));
    }

    @Test
    void asTextUsesEmptyDefaultAndAsTextOrKeepsCustomDefault() throws Exception {
        var root = JsonUtils.getObjectMapper().readTree("{\"msgtype\":\"text\",\"text\":{\"content\":\"hi\"}}");
        assertEquals("text", JsonNodeUtils.asText(root, "msgtype"));
        assertEquals("hi", JsonNodeUtils.asText(root, "text", "content"));
        assertEquals("", JsonNodeUtils.asText(root, "missing"));
        assertNull(JsonNodeUtils.asTextOr(root, null, "msgid"));
        assertEquals("unknown", JsonNodeUtils.asTextOr(root, "unknown", "event"));
    }

    @Test
    void firstTextPicksFirstNonBlankSibling() throws Exception {
        var body = JsonUtils.getObjectMapper().readTree("{\"botid\":\"b1\",\"aibotid\":\"\"}");
        assertEquals("b1", JsonNodeUtils.firstText(body, "", "aibotid", "botid"));
        var event = JsonUtils.getObjectMapper().readTree("{\"event_type\":\"enter_chat\"}");
        assertEquals("enter_chat", JsonNodeUtils.firstText(event, "unknown", "eventtype", "event_type"));
        assertEquals("fallback", JsonNodeUtils.firstText(event, "fallback", "missing"));
    }

    @Test
    void numericAndBooleanReadersUseDefaults() throws Exception {
        var root = JsonUtils.getObjectMapper().readTree("{\"code\":0,\"total\":9,\"ok\":true}");
        assertEquals(0, JsonNodeUtils.asInt(root, -1, "code"));
        assertEquals(-1, JsonNodeUtils.asInt(root, -1, "missing"));
        assertEquals(9L, JsonNodeUtils.asLong(root, 0L, "total"));
        assertTrue(JsonNodeUtils.asBoolean(root, false, "ok"));
        assertFalse(JsonNodeUtils.asBoolean(root, false, "missing"));
    }
}
