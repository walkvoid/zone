package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

@Data
public class BankFileBuildCallbackRequest {

    private String bankOrderNo;
    private String status;
    private String message;
    private String bankCode;
    private String bankName;
    private String customerName;
    private String customerIdCard;
    private String fileBuildTime;
}