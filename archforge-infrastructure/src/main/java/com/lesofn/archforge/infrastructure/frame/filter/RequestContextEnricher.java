package com.lesofn.archforge.infrastructure.frame.filter;

import com.lesofn.archforge.common.context.ClientVersion;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.starter.requestlog.RequestLogEnricher;
import com.lesofn.archforge.starter.requestlog.RequestLogRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 把 RequestContext（ScopedValue 绑定）里的归属信息回填进请求日志记录：
 * uid / source(appId) / ip / clientVersion。无上下文绑定时（如独立使用
 * starter 的场景）静默跳过。
 */
@Component
public class RequestContextEnricher implements RequestLogEnricher {

    @Override
    public void enrich(RequestLogRecord record, HttpServletRequest request) {
        RequestContext context = ScopedValueContext.getRequestContext();
        if (context == null) {
            return;
        }
        record.setUid(context.getCurrentUid());
        record.setSource(context.getAppId() + "");
        record.setIp(context.getIp());
        ClientVersion clientVersion = context.getClientVersion();
        if (clientVersion != null) {
            record.setClientVersion(clientVersion.toString());
        }
    }
}
