package com.github.walkvoid.zone.finance.business.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FinanceTestJob {

    private static final Logger log = LoggerFactory.getLogger(FinanceTestJob.class);

    @XxlJob("financeTestJobHandler")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        log.info("======= financeTestJobHandler 开始执行 =======");
        log.info("调度参数: {}", param);

        XxlJobHelper.log("financeTestJobHandler 执行中...");
        log.info("当前时间: {}", java.time.LocalDateTime.now());

        XxlJobHelper.handleSuccess("执行成功！参数: " + param);
        log.info("======= financeTestJobHandler 执行完毕 =======");
    }
}
