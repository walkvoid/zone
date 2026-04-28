package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

@Data
public class BankFileBuildCallbackResponse {

    private String respCode;
    private String respMsg;
    private String processTime;
    private String platformOrderNo;
}