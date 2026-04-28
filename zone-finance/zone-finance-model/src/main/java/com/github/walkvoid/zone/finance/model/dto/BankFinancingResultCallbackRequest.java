package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

@Data
public class BankFinancingResultCallbackRequest {

    private String bankOrderNo;
    private String financingStatus;
    private String financingAmount;
    private String currency;
    private String interestRate;
    private String loanPeriod;
    private String approvalTime;
    private String rejectReason;
}