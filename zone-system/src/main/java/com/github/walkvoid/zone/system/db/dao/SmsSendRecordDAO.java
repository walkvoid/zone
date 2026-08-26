package com.github.walkvoid.zone.system.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.walkvoid.zone.system.db.entity.SmsSendRecord;
import com.github.walkvoid.zone.system.db.mapper.SmsSendRecordMapper;
import com.github.walkvoid.zone.system.model.enums.SmsBizTypeEnum;
import com.github.walkvoid.zone.system.model.enums.SmsSendStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class SmsSendRecordDAO {

    @Autowired
    private SmsSendRecordMapper smsSendRecordMapper;

    public int insert(SmsSendRecord record) {
        return smsSendRecordMapper.insert(record);
    }

    public int updateById(SmsSendRecord record) {
        return smsSendRecordMapper.updateById(record);
    }

    public SmsSendRecord selectLatestByTargetAndBiz(String target, SmsBizTypeEnum bizType) {
        QueryWrapper<SmsSendRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("target", target)
                .eq("biz_type", bizType.name())
                .orderByDesc("id")
                .last("LIMIT 1");
        return smsSendRecordMapper.selectOne(wrapper);
    }

    public SmsSendRecord selectLatestValidSent(String target, SmsBizTypeEnum bizType, LocalDateTime now) {
        QueryWrapper<SmsSendRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("target", target)
                .eq("biz_type", bizType.name())
                .eq("status", SmsSendStatusEnum.SENT.name())
                .gt("expire_time", now)
                .orderByDesc("id")
                .last("LIMIT 1");
        return smsSendRecordMapper.selectOne(wrapper);
    }

    public long countSentSince(String target, SmsBizTypeEnum bizType, LocalDateTime since) {
        QueryWrapper<SmsSendRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("target", target)
                .eq("biz_type", bizType.name())
                .ge("create_time", since)
                .in("status", SmsSendStatusEnum.SENT.name(), SmsSendStatusEnum.USED.name(),
                        SmsSendStatusEnum.EXPIRED.name());
        return smsSendRecordMapper.selectCount(wrapper);
    }

    public int expirePreviousSent(String target, SmsBizTypeEnum bizType, Long excludeId) {
        UpdateWrapper<SmsSendRecord> wrapper = new UpdateWrapper<>();
        wrapper.eq("target", target)
                .eq("biz_type", bizType.name())
                .eq("status", SmsSendStatusEnum.SENT.name());
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        wrapper.set("status", SmsSendStatusEnum.EXPIRED.name())
                .set("update_time", LocalDateTime.now());
        return smsSendRecordMapper.update(null, wrapper);
    }
}
