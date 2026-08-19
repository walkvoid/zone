package com.github.walkvoid.zone.ai.business.tool.sql;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedSqlBinderTest {

    private final NamedSqlQueryCatalog catalog = new NamedSqlQueryCatalog();
    private final Set<String> allowed = new HashSet<>(NamedSqlQueryCatalog.DEFAULT_ALLOWED_TABLES);

    @Test
    void bindsLikeAndLimit() throws Exception {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("by", "real_name");
        params.put("value", "青");
        NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(catalog.get("user_info"), params, 50, allowed);
        assertTrue(bound.sql().contains("real_name LIKE"));
        assertTrue(bound.sql().endsWith(" LIMIT 50"));
        assertEquals(1, bound.args().length);
        assertEquals("%青%", bound.args()[0]);
    }

    @Test
    void expandsInList() throws Exception {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("value", "YSZC2024114403,YSZC2024114402");
        NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(catalog.get("asset_info"), params, 20, allowed);
        assertTrue(bound.sql().contains("IN (?,?)"));
        assertEquals(2, bound.args().length);
        assertEquals("YSZC2024114403", bound.args()[0]);
        assertEquals("YSZC2024114402", bound.args()[1]);
    }

    @Test
    void recentCustomerNeedsNoValue() {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("by", "recent");
        NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(catalog.get("cust_company"), params, 100, allowed);
        assertTrue(bound.sql().toUpperCase().contains("ORDER BY ID DESC"));
        assertEquals(0, bound.args().length);
        assertTrue(bound.sql().endsWith(" LIMIT 100"));
    }

    @Test
    void requiresByWhenVariantsExist() {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("value", "1");
        assertThrows(IllegalArgumentException.class,
                () -> NamedSqlBinder.bind(catalog.get("user_info"), params, 10, allowed));
    }

    @Test
    void tradeRelByNameUsesJoin() {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("by", "contract_name");
        params.put("value", "5030-卓然");
        NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(catalog.get("trade_contract_rel"), params, 10, allowed);
        assertTrue(bound.sql().toUpperCase().contains("INNER JOIN"));
        assertEquals("%5030-卓然%", bound.args()[0]);
    }

    @Test
    void httpRequestLogByChannel() {
        ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
        params.put("by", "channel");
        params.put("interfaceCode", "creditLoanApply");
        params.put("channel", "icbc-eloan");
        NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(catalog.get("http_request_log"), params, 50, allowed);
        assertEquals(2, bound.args().length);
        assertEquals("creditLoanApply", bound.args()[0]);
        assertEquals("icbc-eloan", bound.args()[1]);
    }

    @Test
    void catalogCodesAreUniqueAndBindable() {
        Set<String> codes = new HashSet<>();
        for (NamedSqlQuery query : catalog.all()) {
            assertTrue(codes.add(query.code()), "duplicate code " + query.code());
            query.variants().forEach((by, variant) -> {
                ObjectNode params = JsonUtils.getObjectMapper().createObjectNode();
                if (!"default".equals(by)) {
                    params.put("by", by);
                }
                for (NamedSqlQuery.Bind bind : variant.binds()) {
                    params.put(bind.name(), "v1,v2");
                }
                NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(query, params, 10, allowed);
                assertFalse(bound.sql().contains("{in:"));
                assertTrue(bound.sql().toUpperCase().contains("LIMIT"));
            });
        }
        assertFalse(codes.isEmpty());
    }
}
