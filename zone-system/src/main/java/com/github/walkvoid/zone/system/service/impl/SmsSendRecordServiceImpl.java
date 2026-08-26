package com.github.walkvoid.zone.system.service.impl;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.system.db.dao.SmsSendRecordDAO;
import com.github.walkvoid.zone.system.db.entity.SmsSendRecord;
import com.github.walkvoid.zone.system.model.enums.SmsBizTypeEnum;
import com.github.walkvoid.zone.system.model.enums.SmsSendStatusEnum;
import com.github.walkvoid.zone.system.service.SmsSendRecordService;
import com.github.walkvoid.zone.system.service.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SmsSendRecordServiceImpl implements SmsSendRecordService {

    private static final int CODE_TTL_MINUTES = 5;
    private static final int RESEND_INTERVAL_SECONDS = 60;
    private static final int DAILY_LIMIT = 20;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private SmsSendRecordDAO smsSendRecordDAO;
    @Autowired
    private SmsSender smsSender;

    @Override
    public ApiResult<Void> sendCode(String target, SmsBizTypeEnum bizType, String clientIp) {
        if (!StringUtils.hasText(target) || bizType == null) {
            return ApiResult.error(400, "手机号与业务类型不能为空");
        }
        String phone = target.trim();
        LocalDateTime now = LocalDateTime.now();

        SmsSendRecord latest = smsSendRecordDAO.selectLatestByTargetAndBiz(phone, bizType);
        if (latest != null && latest.getCreateTime() != null
                && latest.getCreateTime().isAfter(now.minusSeconds(RESEND_INTERVAL_SECONDS))) {
            return ApiResult.error(429, "发送过于频繁，请稍后再试");
        }

        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long dayCount = smsSendRecordDAO.countSentSince(phone, bizType, dayStart);
        if (dayCount >= DAILY_LIMIT) {
            return ApiResult.error(429, "今日发送次数已达上限");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        SmsSendRecord record = new SmsSendRecord();
        record.setBizType(bizType);
        record.setTarget(phone);
        record.setChannel("SMS");
        record.setCode(code);
        record.setClientIp(clientIp);
        record.setExpireTime(now.plusMinutes(CODE_TTL_MINUTES));
        record.setCreateTime(now);
        record.setUpdateTime(now);

        try {
            String msgId = smsSender.send(phone, code, bizType.name());
            record.setProviderMsgId(msgId);
            record.setStatus(SmsSendStatusEnum.SENT);
            smsSendRecordDAO.insert(record);
            smsSendRecordDAO.expirePreviousSent(phone, bizType, record.getId());
            return ApiResult.ok(null);
        } catch (Exception e) {
            record.setStatus(SmsSendStatusEnum.FAILED);
            record.setFailReason(truncate(e.getMessage(), 250));
            smsSendRecordDAO.insert(record);
            return ApiResult.error(500, "短信发送失败");
        }
    }

    @Override
    public ApiResult<Boolean> verifyCode(String target, SmsBizTypeEnum bizType, String code) {
        if (!StringUtils.hasText(target) || bizType == null || !StringUtils.hasText(code)) {
            return ApiResult.error(400, "参数不完整");
        }
        String phone = target.trim();
        String inputCode = code.trim();
        LocalDateTime now = LocalDateTime.now();

        SmsSendRecord record = smsSendRecordDAO.selectLatestValidSent(phone, bizType, now);
        if (record == null) {
            return ApiResult.error(400, "验证码无效或已过期");
        }
        if (!inputCode.equals(record.getCode())) {
            return ApiResult.error(400, "验证码错误");
        }

        record.setStatus(SmsSendStatusEnum.USED);
        record.setUsedTime(now);
        record.setUpdateTime(now);
        smsSendRecordDAO.updateById(record);
        return ApiResult.ok(Boolean.TRUE);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
