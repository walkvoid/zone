package com.github.walkvoid.zone.ai.business.tool.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一条命名查询：模型只传 code + by + value，SQL 固定在目录里。
 */
public final class NamedSqlQuery {

    public enum BindKind {
        EQ, LIKE, IN
    }

    public static final class Bind {
        private final String name;
        private final BindKind kind;

        public Bind(String name, BindKind kind) {
            this.name = name;
            this.kind = kind;
        }

        public String name() {
            return name;
        }

        public BindKind kind() {
            return kind;
        }
    }

    public static final class Variant {
        private final String sql;
        private final List<Bind> binds;
        private final List<String> tables;

        public Variant(String sql, List<Bind> binds, List<String> tables) {
            this.sql = sql;
            this.binds = List.copyOf(binds);
            this.tables = List.copyOf(tables);
        }

        public String sql() {
            return sql;
        }

        public List<Bind> binds() {
            return binds;
        }

        public List<String> tables() {
            return tables;
        }
    }

    private final String code;
    private final String category;
    private final String description;
    private final Map<String, Variant> variants;

    public NamedSqlQuery(String code, String category, String description, Map<String, Variant> variants) {
        this.code = code;
        this.category = category;
        this.description = description;
        this.variants = Map.copyOf(variants);
    }

    public String code() {
        return code;
    }

    public String category() {
        return category;
    }

    public String description() {
        return description;
    }

    public Map<String, Variant> variants() {
        return variants;
    }

    public List<String> byOptions() {
        List<String> keys = new ArrayList<>(variants.keySet());
        keys.remove("default");
        return keys;
    }

    public static Builder of(String code, String category, String description) {
        return new Builder(code, category, description);
    }

    public static final class Builder {
        private final String code;
        private final String category;
        private final String description;
        private final Map<String, Variant> variants = new LinkedHashMap<>();
        private List<String> tables = List.of();

        private Builder(String code, String category, String description) {
            this.code = code;
            this.category = category;
            this.description = description;
        }

        public Builder tables(String... tables) {
            this.tables = List.of(tables);
            return this;
        }

        public Builder variant(String by, String sql, Bind... binds) {
            variants.put(by, new Variant(sql, List.of(binds), tables));
            return this;
        }

        public NamedSqlQuery build() {
            if (variants.isEmpty()) {
                throw new IllegalStateException("named query has no variants: " + code);
            }
            return new NamedSqlQuery(code, category, description, variants);
        }
    }

    public static Bind eq(String name) {
        return new Bind(name, BindKind.EQ);
    }

    public static Bind like(String name) {
        return new Bind(name, BindKind.LIKE);
    }

    public static Bind in(String name) {
        return new Bind(name, BindKind.IN);
    }
}
