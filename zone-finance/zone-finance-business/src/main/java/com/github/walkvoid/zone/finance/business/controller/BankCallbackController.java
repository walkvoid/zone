package com.github.walkvoid.zone.finance.business.controller;

import com.github.walkvoid.zone.finance.model.dto.BankFileBuildCallbackRequest;
import com.github.walkvoid.zone.finance.model.dto.BankFileBuildCallbackResponse;
import com.github.walkvoid.zone.finance.model.dto.BankFinancingResultCallbackRequest;
import com.github.walkvoid.zone.finance.model.dto.BankFinancingResultCallbackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/12/5
 * @desc 银行回调controller
 */
@RestController
@RequestMapping("/api/finance/bank/callback")
public class BankCallbackController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 银行建档回调接口
     * @param request 建档回调请求
     * @return 建档回调响应
     */
    @PostMapping("/file-build")
    public ResponseEntity<BankFileBuildCallbackResponse> fileBuildCallback(@RequestBody BankFileBuildCallbackRequest request) {
        // 记录回调日志（实际项目中应该使用日志框架）
        System.out.println("收到银行建档回调请求：" + request);

        // 构建响应对象
        BankFileBuildCallbackResponse response = new BankFileBuildCallbackResponse();
        response.setRespCode("0000");
        response.setRespMsg("成功");
        response.setProcessTime(LocalDateTime.now().format(formatter));
        response.setPlatformOrderNo(request.getBankOrderNo()); // 实际项目中应该从数据库查询平台订单号

        // TODO: 实现建档回调业务逻辑
        // 1. 验证银行回调请求的签名
        // 2. 根据银行订单号查询对应的平台订单
        // 3. 更新订单状态为建档成功/失败
        // 4. 发送消息通知其他系统

        return ResponseEntity.ok(response);
    }

    /**
     * 银行融资结果回调接口
     * @param request 融资结果回调请求
     * @return 融资结果回调响应
     */
    @PostMapping("/financing-result")
    public ResponseEntity<BankFinancingResultCallbackResponse> financingResultCallback(@RequestBody BankFinancingResultCallbackRequest request) {
        // 记录回调日志（实际项目中应该使用日志框架）
        System.out.println("收到银行融资结果回调请求：" + request);

        // 构建响应对象
        BankFinancingResultCallbackResponse response = new BankFinancingResultCallbackResponse();
        response.setRespCode("0000");
        response.setRespMsg("成功");
        response.setProcessTime(LocalDateTime.now().format(formatter));
        response.setAcceptTime(LocalDateTime.now().format(formatter));
        response.setPlatformOrderNo(request.getBankOrderNo()); // 实际项目中应该从数据库查询平台订单号

        // TODO: 实现融资结果回调业务逻辑
        // 1. 验证银行回调请求的签名
        // 2. 根据银行订单号查询对应的平台订单
        // 3. 更新订单的融资结果状态
        // 4. 如果融资成功，更新账户余额
        // 5. 发送消息通知其他系统

        return ResponseEntity.ok(response);
    }
}