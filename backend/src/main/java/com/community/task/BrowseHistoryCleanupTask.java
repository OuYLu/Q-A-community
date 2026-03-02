package com.community.task;

import com.community.mapper.UserBrowseHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrowseHistoryCleanupTask {
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;

    @Value("${qa.history-retention-days:30}")
    private int historyRetentionDays;

    /**
     * 每天凌晨执行一次，清理超过保留期的浏览历史，并清理失效目标的残留记录。
     */
    @Scheduled(cron = "${qa.history-cleanup-cron:0 15 3 * * ?}")
    @Transactional
    public void cleanup() {
        int retentionDays = Math.max(1, historyRetentionDays);
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(retentionDays);
        int deletedOld = userBrowseHistoryMapper.deleteOlderThan(expireBefore);
        int deletedInvalid = userBrowseHistoryMapper.deleteInvalidRows();
        if (deletedOld > 0 || deletedInvalid > 0) {
            log.info("browse history cleanup done, retentionDays={}, deletedOld={}, deletedInvalid={}",
                retentionDays, deletedOld, deletedInvalid);
        } else {
            log.debug("browse history cleanup done, nothing deleted, retentionDays={}", retentionDays);
        }
    }
}
