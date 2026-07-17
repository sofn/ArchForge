package com.lesofn.archforge.server.admin.listener;

import com.lesofn.archforge.server.admin.event.LogEvent;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import com.lesofn.archforge.user.api.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Asynchronous listener that persists audit logs in a separate thread.
 *
 * <p>
 * {@link TransactionalEventListener} with {@code AFTER_COMMIT} ensures logs are only written
 * when the triggering transaction commits; {@code fallbackExecution = true} keeps it working
 * for non-transactional controller methods.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final SysOperLogService operLogService;

    @Async("logTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLogEvent(LogEvent event) {
        SysOperLog operLog = event.getOperLog();
        if (operLog == null) {
            return;
        }
        try {
            operLogService.create(operLog);
        } catch (Exception e) {
            log.warn("Failed to persist operation log asynchronously", e);
        }
    }
}
