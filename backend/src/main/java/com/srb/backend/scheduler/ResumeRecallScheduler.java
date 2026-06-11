package com.srb.backend.scheduler;

import com.srb.backend.config.ResumeRecallProperties;
import com.srb.backend.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeRecallScheduler {

    private static final String RECALL_SCAN_LOCK = "lock:resume:recall:scan";

    private final NoticeService noticeService;
    private final ResumeRecallProperties resumeRecallProperties;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "${resume.recall.scan-cron}")
    public void scanRecallEmails() {
        if (!resumeRecallProperties.isEnabled()) {
            return;
        }

        RLock lock = redissonClient.getLock(RECALL_SCAN_LOCK);
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (!locked) {
                log.info("跳过简历召回扫描，本轮未获取到分布式锁");
                return;
            }
            noticeService.scanAndSendRecallEmails();
        } catch (Exception e) {
            log.error("简历召回扫描执行失败", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
