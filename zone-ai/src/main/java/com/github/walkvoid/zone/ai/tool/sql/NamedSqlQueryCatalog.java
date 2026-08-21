package com.github.walkvoid.zone.ai.tool.sql;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.walkvoid.zone.ai.tool.sql.NamedSqlQuery.eq;
import static com.github.walkvoid.zone.ai.tool.sql.NamedSqlQuery.in;
import static com.github.walkvoid.zone.ai.tool.sql.NamedSqlQuery.like;
import static com.github.walkvoid.zone.ai.tool.sql.NamedSqlQuery.of;

/**
 * jinkoscf 常用只读查询目录。同一类 SQL 合并为一条，用 by 区分条件。
 */
@Component
public class NamedSqlQueryCatalog {

    public static final List<String> DEFAULT_ALLOWED_TABLES = List.of(
            "jinkoscf_tools.auth_role",
            "jinkoscf_tools.user_info",
            "jinkoscf_tools.sso_account",
            "jinkoscf_tools.user_cust_rel",
            "jinkoscf_tools.user_cust_role_rel",
            "jinkoscf_tools.media_file",
            "jinkoscf_business_common.contract_info",
            "jinkoscf_business_common.contract_sign_info",
            "jinkoscf_business_common.contract_trade_relation",
            "jinkoscf_business_common.contract_trade_info",
            "jinkoscf_business_common.invoice_business_rel",
            "jinkoscf_business_common.cust_company_info",
            "jinkoscf_business_common.apply_cust_company_info",
            "jinkoscf_business_common.cust_role_info",
            "jinkoscf_business_common.asset_info",
            "jinkoscf_business_common.limit_info",
            "jinkoscf_transaction.ts_asset",
            "jinkoscf_transaction.ts_asset_abolish_apply",
            "jinkoscf_transaction.ts_transaction",
            "jinkoscf_transaction.ts_cash_fee",
            "jinkoscf_transaction.pay_trade",
            "jinkoscf_transaction.pay_trade_funder_record",
            "jinkoscf_gateway.thirdparty_channel_invoked_log",
            "jinkoscf_gateway.http_mock_rule",
            "jinkoscf_gateway.http_request_log"
    );

    private final List<NamedSqlQuery> queries;
    private final Map<String, NamedSqlQuery> byCode;

    public NamedSqlQueryCatalog() {
        this.queries = List.copyOf(build());
        Map<String, NamedSqlQuery> map = new LinkedHashMap<>();
        for (NamedSqlQuery q : queries) {
            if (map.put(q.code(), q) != null) {
                throw new IllegalStateException("duplicate named query code: " + q.code());
            }
        }
        this.byCode = Map.copyOf(map);
    }

    public List<NamedSqlQuery> all() {
        return queries;
    }

    public NamedSqlQuery get(String code) {
        NamedSqlQuery query = byCode.get(code);
        if (query == null) {
            throw new IllegalArgumentException("unknown queryCode: " + code + ", call listNamedQueries first");
        }
        return query;
    }

    private static List<NamedSqlQuery> build() {
        return List.of(
                of("user_info", "用户",
                        "查用户。params: by=id|real_name|user_name, value=对应值。real_name（通常为中文名称）/user_name（通常为手机号） 为模糊匹配。")
                        .tables("jinkoscf_tools.user_info")
                        .variant("id",
                                "SELECT * FROM jinkoscf_tools.user_info WHERE id = ?", eq("value"))
                        .variant("real_name",
                                "SELECT * FROM jinkoscf_tools.user_info WHERE real_name LIKE ? ESCAPE '\\\\'", like("value"))
                        .variant("user_name",
                                "SELECT * FROM jinkoscf_tools.user_info WHERE user_name LIKE ? ESCAPE '\\\\'", like("value"))
                        .build(),
                of("auth_role", "用户",
                        "查角色。params: value=id")
                        .tables("jinkoscf_tools.auth_role")
                        .variant("default",
                                "SELECT * FROM jinkoscf_tools.auth_role WHERE id = ?", eq("value"))
                        .build(),

                of("user_cust_rel", "用户",
                        "查用户-企业关系。params: by=company_id|user_id, value=对应值")
                        .tables("jinkoscf_tools.user_cust_rel")
                        .variant("company_id",
                                "SELECT * FROM jinkoscf_tools.user_cust_rel WHERE company_id = ?", eq("value"))
                        .variant("user_id",
                                "SELECT * FROM jinkoscf_tools.user_cust_rel WHERE user_id = ?", eq("value"))
                        .build(),

                of("user_cust_role_rel", "用户",
                        "查用户-企业-角色关系。params: by=company_id|user_id, value=对应值")
                        .tables("jinkoscf_tools.user_cust_role_rel")
                        .variant("company_id",
                                "SELECT * FROM jinkoscf_tools.user_cust_role_rel WHERE company_id = ?", eq("value"))
                        .variant("user_id",
                                "SELECT * FROM jinkoscf_tools.user_cust_role_rel WHERE user_id = ?", eq("value"))
                        .build(),

                of("media_file", "影像件",
                        "查已启用影像件。params: value=busi_key通常是交易编号和凭证编号")
                        .tables("jinkoscf_tools.media_file")
                        .variant("default",
                                "SELECT * FROM jinkoscf_tools.media_file WHERE busi_key = ? AND `enable` = 1", eq("value"))
                        .build(),

                of("contract_info", "合同",
                        "查合同。params: by=app_no|business_type, value=对应值。business_type 支持逗号分隔。")
                        .tables("jinkoscf_business_common.contract_info")
                        .variant("app_no",
                                "SELECT * FROM jinkoscf_business_common.contract_info WHERE app_no = ?", eq("value"))
                        .variant("business_type",
                                "SELECT * FROM jinkoscf_business_common.contract_info WHERE business_type IN ({in:value})", in("value"))
                        .build(),

                of("contract_sign", "合同",
                        "查合同签署。params: value=app_no")
                        .tables("jinkoscf_business_common.contract_sign_info")
                        .variant("default",
                                "SELECT * FROM jinkoscf_business_common.contract_sign_info WHERE app_no = ?", eq("value"))
                        .build(),

                of("trade_contract", "贸易合同",
                        "贸易合同。params: by=contract_code|contract_name, value=对应值。contract_name 为模糊匹配。")
                        .tables("jinkoscf_business_common.contract_trade_info")
                        .variant("contract_code",
                                "SELECT * FROM jinkoscf_business_common.contract_trade_info WHERE contract_code = ?", eq("value"))
                        .variant("contract_name",
                                "SELECT * FROM jinkoscf_business_common.contract_trade_info WHERE contract_name LIKE ? ESCAPE '\\\\'", like("value"))
                        .build(),

                of("trade_contract_rel", "贸易合同",
                        "查贸易合同关系。params: by=related_business_no|source_id|contract_name, value=对应值。"
                                + "source_id 支持逗号分隔；contract_name 按合同名称模糊关联。")
                        .tables("jinkoscf_business_common.contract_trade_relation",
                                "jinkoscf_business_common.contract_trade_info")
                        .variant("related_business_no",
                                "SELECT * FROM jinkoscf_business_common.contract_trade_relation WHERE related_business_no = ?",
                                eq("value"))
                        .variant("source_id",
                                "SELECT * FROM jinkoscf_business_common.contract_trade_relation WHERE source_id IN ({in:value})",
                                in("value"))
                        .variant("contract_name",
                                "SELECT r.* FROM jinkoscf_business_common.contract_trade_relation r "
                                        + "INNER JOIN jinkoscf_business_common.contract_trade_info i ON r.source_id = i.id "
                                        + "WHERE i.contract_name LIKE ? ESCAPE '\\\\'",
                                like("value"))
                        .build(),

                of("invoice_rel", "发票",
                        "查发票业务关联。params: value=busi_key")
                        .tables("jinkoscf_business_common.invoice_business_rel")
                        .variant("default",
                                "SELECT * FROM jinkoscf_business_common.invoice_business_rel WHERE busi_key = ?", eq("value"))
                        .build(),

                of("cust_company", "客户",
                        "查客户企业。params: by=id|cust_no|cust_name|recent, value=对应值（recent 不需要 value）。cust_name 为模糊匹配。")
                        .tables("jinkoscf_business_common.cust_company_info")
                        .variant("id",
                                "SELECT * FROM jinkoscf_business_common.cust_company_info WHERE id = ?", eq("value"))
                        .variant("cust_no",
                                "SELECT * FROM jinkoscf_business_common.cust_company_info WHERE cust_no = ?", eq("value"))
                        .variant("cust_name",
                                "SELECT * FROM jinkoscf_business_common.cust_company_info WHERE cust_name LIKE ? ESCAPE '\\\\'", like("value"))
                        .variant("recent",
                                "SELECT * FROM jinkoscf_business_common.cust_company_info ORDER BY id DESC")
                        .build(),

                of("apply_cust_company", "客户",
                        "查进件客户。params: by=cust_name|app_no, value=对应值。cust_name 为精确匹配。")
                        .tables("jinkoscf_business_common.apply_cust_company_info")
                        .variant("cust_name",
                                "SELECT * FROM jinkoscf_business_common.apply_cust_company_info WHERE cust_name = ?", eq("value"))
                        .variant("app_no",
                                "SELECT * FROM jinkoscf_business_common.apply_cust_company_info WHERE app_no = ?", eq("value"))
                        .build(),

                of("cust_role", "客户",
                        "查客户角色。params: value=cust_id")
                        .tables("jinkoscf_business_common.cust_role_info")
                        .variant("default",
                                "SELECT * FROM jinkoscf_business_common.cust_role_info WHERE cust_id = ?", eq("value"))
                        .build(),

                of("asset_info", "资产",
                        "查原始资产。params: by=id（资产id）|asset_no(资产编号), value=对应值,支持逗号分隔。")
                        .tables("jinkoscf_business_common.asset_info")
                        .variant("id",
                                "SELECT * FROM jinkoscf_business_common.asset_info WHERE id = ?", eq("value"))
                        .variant("asset_no",
                                "SELECT * FROM jinkoscf_business_common.asset_info WHERE asset_no IN ({in:value})", in("value"))
                        .variant("default",
                                "SELECT * FROM jinkoscf_business_common.asset_info WHERE asset_no IN ({in:value})", in("value"))
                        .build(),
                of("limit_info", "额度",
                        "查额度。params: value=app_limit_no")
                        .tables("jinkoscf_business_common.limit_info")
                        .variant("default",
                                "SELECT * FROM jinkoscf_business_common.limit_info WHERE app_limit_no = ?", eq("value"))
                        .build(),

                of("ts_asset", "凭证",
                        "查交易凭证。params: by=id|asset_no（原始资产id）|ts_asset_no（凭证编号）|status, value=对应值,支持逗号分隔。")
                        .tables("jinkoscf_transaction.ts_asset")
                        .variant("id",
                                "SELECT * FROM jinkoscf_transaction.ts_asset WHERE id = ?", eq("value"))
                        .variant("asset_no",
                                "SELECT * FROM jinkoscf_transaction.ts_asset WHERE asset_no IN ({in:value})", in("value"))
                        .variant("ts_asset_no",
                                "SELECT * FROM jinkoscf_transaction.ts_asset WHERE ts_asset_no = ?", eq("value"))
                        .variant("status",
                                "SELECT * FROM jinkoscf_transaction.ts_asset WHERE status = ?", eq("value"))
                        .build(),

                of("ts_transaction", "交易",
                        "查交易包括融资，提前结清和凭证转让。params: value=id")
                        .tables("jinkoscf_transaction.ts_transaction")
                        .variant("default",
                                "SELECT * FROM jinkoscf_transaction.ts_transaction WHERE id = ?", eq("value"))
                        .build(),

                of("cash_fee", "费用",
                        "查交易费用。params: value=transaction_id")
                        .tables("jinkoscf_transaction.ts_cash_fee")
                        .variant("default",
                                "SELECT * FROM jinkoscf_transaction.ts_cash_fee WHERE transaction_id = ?", eq("value"))
                        .build(),

                of("pay_trade", "兑付",
                        "查兑付（司库支付）。params: by=id|asset_no|ts_asset_no|transaction_id|core_company_id, value=对应值。ts_asset_no 支持逗号分隔。")
                        .tables("jinkoscf_transaction.pay_trade")
                        .variant("id",
                                "SELECT * FROM jinkoscf_transaction.pay_trade WHERE id = ?", eq("value"))
                        .variant("asset_no",
                                "SELECT * FROM jinkoscf_transaction.pay_trade WHERE asset_no = ?", eq("value"))
                        .variant("ts_asset_no",
                                "SELECT * FROM jinkoscf_transaction.pay_trade WHERE ts_asset_no IN ({in:value})", in("value"))
                        .variant("transaction_id",
                                "SELECT * FROM jinkoscf_transaction.pay_trade WHERE transaction_id = ?", eq("value"))
                        .variant("core_company_id",
                                "SELECT * FROM jinkoscf_transaction.pay_trade WHERE core_company_id = ?", eq("value"))
                        .build(),

                of("pay_trade_funder", "兑付",
                        "查兑付后通知资金方记录。params: value=ts_asset_no")
                        .tables("jinkoscf_transaction.pay_trade_funder_record")
                        .variant("default",
                                "SELECT * FROM jinkoscf_transaction.pay_trade_funder_record WHERE ts_asset_no = ?", eq("value"))
                        .build(),

                of("http_request_log", "网关",
                        "查网关请求日志，按创建时间倒序。params: by=interface|channel|body；"
                                + "interfaceCode=接口编码（必填）。channel 查询再传 channel；body 查询再传 value=报文关键字。")
                        .tables("jinkoscf_gateway.http_request_log")
                        .variant("interface",
                                "SELECT * FROM jinkoscf_gateway.http_request_log WHERE interface_code = ? ORDER BY create_time DESC",
                                eq("interfaceCode"))
                        .variant("channel",
                                "SELECT * FROM jinkoscf_gateway.http_request_log WHERE interface_code = ? AND channel = ? ORDER BY create_time DESC",
                                eq("interfaceCode"), eq("channel"))
                        .variant("body",
                                "SELECT * FROM jinkoscf_gateway.http_request_log WHERE interface_code = ? AND coder_request_body LIKE ? ESCAPE '\\\\' ORDER BY create_time DESC",
                                eq("interfaceCode"), like("value"))
                        .build(),
                of("gateway_invoked_log", "网关",
                        "查最近三方通道调用日志。无需 params。")
                        .tables("jinkoscf_gateway.thirdparty_channel_invoked_log")
                        .variant("default",
                                "SELECT * FROM jinkoscf_gateway.thirdparty_channel_invoked_log ORDER BY create_time DESC")
                        .build()
        );
    }
}
