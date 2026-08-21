package com.github.walkvoid.zone.ai.model.enums;

/**
 * Supply chain financing status enumeration.
 * Used for RAG knowledge base to provide status descriptions.
 *
 * @author jiangjunqing
 * @date 2026/8/6
 */
public enum FinancingStatusEnum {

    INIT("待审核", "init"),
    SED_REJECT("发起方拒绝", "sedReject"),
    PROGRESS("交易中", "progress"),
    APP("申请审核中", "app"),
    APP_CMT("申请提交", "appCmt"),
    SIGN_IN_APP("签收审核中", "signInApp"),
    SUCCESS("交易成功", "success"),
    REJECT_APP("拒签处理中", "rejectApp"),
    REJECT("交易失败", "reject"),
    PLATE_APP("平台审核中", "plateApp"),

    // Only used by Zheshang non-direct financing when connecting to capital platform
    FUNDER_CONFIRM("待资金方确认", "funderConfirm"),
    CONFIRM("融资确认", "confirm"),
    FUNDER_APP("资金方审核中", "funderApp"),
    AGW_REJECT("网关拒绝", "agwReject"),
    CANCEL_APP("撤销申请中", "cancelApp"),

    CANCEL("已撤销", "cancel"),
    FAILED("处理失败", "failed"),
    WAIT_SIGN_FOR("待签收", "waitSignFor"),
    APP_TO_BE_SIGNED("申请待签署合同", "appToBeSigned"),
    SIGN_IN_TO_BE_SIGNED("签收待签署合同", "signInToBeSigned"),

    // Used by Bank of Communications financing, Minsheng financing, Zheshang financing
    CONTRACT_TO_BE_SIGN("合同待签署", "contract_to_be_sign"),
    CORE_TO_BE_SIGNED("待核心企业签署合同", "coreToBeSigned"),
    LOAN_APP("放款审核中", "loanApp"),
    BACK_MATERIAL("退回补充资料", "backMaterial");

    private final String desc;
    private final String code;

    FinancingStatusEnum(String desc, String code) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }

    public static FinancingStatusEnum getByCode(String code) {
        for (FinancingStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
