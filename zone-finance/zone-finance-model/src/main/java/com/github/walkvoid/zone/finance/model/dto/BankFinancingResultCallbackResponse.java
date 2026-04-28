package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

@Data
public class BankFinancingResultCallbackResponse {

    private String respCode;
    private String respMsg;
    private String processTime;
    private String acceptTime;
    private String platformOrderNo;
}