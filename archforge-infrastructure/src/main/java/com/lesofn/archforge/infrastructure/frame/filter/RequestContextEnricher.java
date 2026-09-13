package com.lesofn.archforge.infrastructure.frame.filter;

import cn.dev33.satoken.stp.StpLogic;
import com.lesofn.archforge.common.context.ClientVersion;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.starter.requestlog.RequestLogEnricher;
import com.lesofn.archforge.starter.requestlog.RequestLogRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
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
        // sa-token's request context lives inside its own filter scope, which this enricher
        // out-ranks — resolve uid straight from the Bearer token's loginId mapping instead.
        record.setUid(resolveUid(request));
        record.setSource(context.getAppId() + "");
        record.setIp(context.getIp());
        ClientVersion clientVersion = context.getClientVersion();
        if (clientVersion != null) {
            record.setClientVersion(clientVersion.toString());
        }
    }

    private static long resolveUid(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            Long uid = uidByToken(token, StpAdminUtil.STP_LOGIC);
            if (uid == null) {
                uid = uidByToken(token, StpWebUtil.STP_LOGIC);
            }
            if (uid != null) {
                return uid;
            }
        }
        return 0;
    }

    private static @Nullable Long uidByToken(String token, StpLogic stpLogic) {
        try {
            Object loginId = stpLogic.getLoginIdByToken(token);
            if (loginId instanceof Number number) {
                return number.longValue();
            }
            if (loginId != null) {
                return Long.valueOf(String.valueOf(loginId));
            }
        } catch (RuntimeException ignored) {
            // invalid/expired token — log line keeps uid=0
        }
        return null;
    }
}
