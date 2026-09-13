package com.lesofn.archforge.starter.requestlog;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * 默认日志落点：MDC 打 CUSTOM_LOG=request 标记后 INFO 输出；
 * 超长记录降级为摘要行（沿用既有语义）。
 */
@Slf4j
public class LoggingRequestLogSink implements Consumer<RequestLogRecord> {

    private final int maxPayloadLength;

    public LoggingRequestLogSink(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public void accept(RequestLogRecord record) {
        MDC.put("CUSTOM_LOG", "request");
        try {
            String recordString = record.toString();
            if (recordString.length() > maxPayloadLength) {
                log.info(
                        "Output too long, ignoring detailed log output. RequestId: {}, API: {}, Method: {}, Status: {}, UseTime: {}ms",
                        record.getRequestId(),
                        record.getApi(),
                        record.getMethod(),
                        record.getResponseStatus(),
                        record.getUseTime());
            } else {
                log.info(recordString);
            }
        } finally {
            MDC.remove("CUSTOM_LOG");
        }
    }
}
