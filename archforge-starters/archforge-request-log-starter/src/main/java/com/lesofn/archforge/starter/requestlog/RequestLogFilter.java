package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求日志过滤器：打印全部请求参数（query/form/JSON body）与响应结果。
 *
 * <p>
 * 载荷捕获自包含：请求体经 {@link RequestLogRequestWrapper} eager 缓存后可重读；
 * 响应体经 {@link RequestLogResponseWrapper} tee 镜像。敏感字段按
 * {@code arch-forge.request-log.mask-fields} 脱敏为 {@code ***}。
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private static final String MASK = "***";
    private static final String ERROR_URI_ATTRIBUTE = "jakarta.servlet.error.request_uri";

    private final RequestLogProperties properties;
    private final ObjectProvider<RequestLogEnricher> enrichers;
    private final Consumer<RequestLogRecord> sink;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestLogFilter(RequestLogProperties properties, ObjectProvider<RequestLogEnricher> enrichers,
            Consumer<RequestLogRecord> sink) {
        this.properties = properties;
        this.enrichers = enrichers;
        this.sink = sink;
    }

    /** 保留旧语义：/error 转发也产一条记录（api 回填原始 URI）。 */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest requestToUse = wrapRequest(request);
        HttpServletResponse responseToUse = properties.isIncludeResponsePayload()
                ? new RequestLogResponseWrapper(response)
                : response;
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            writeRecord(request, requestToUse, responseToUse, path, startTime);
        }
    }

    private void writeRecord(HttpServletRequest original, HttpServletRequest request, HttpServletResponse response,
            String path, long startTime) {
        RequestLogRecord record = new RequestLogRecord();
        record.setRequestId(MDC.get("requestId"));
        record.setMethod(request.getMethod());
        record.setUseTime(System.currentTimeMillis() - startTime);
        record.setResponseStatus(response.getStatus());
        record.setParameters(maskParameters(request.getParameterMap()));
        record.setUserAgent(request.getHeader("User-Agent"));
        record.setIp(realIp(request));
        Object errorUri = original.getAttribute(ERROR_URI_ATTRIBUTE);
        record.setApi(errorUri instanceof String uri ? uri : path);

        if (request instanceof RequestLogRequestWrapper wrapper && wrapper.getBody().length > 0) {
            record.setPayload(maskPayload(sanitize(new String(wrapper.getBody(), StandardCharsets.UTF_8))));
        }
        if (response instanceof RequestLogResponseWrapper wrapper) {
            // 非 JSON 响应不打印 body（与旧行为一致）
            if (Strings.CS.contains(response.getContentType(), "application/json")) {
                record.setResponse(maskPayload(sanitize(new String(wrapper.toByteArray(), StandardCharsets.UTF_8))));
            } else {
                record.setResponse("");
                record.setWriteBody(false);
            }
        } else {
            record.setResponse("");
            record.setWriteBody(false);
        }
        enrichers.orderedStream().forEach(enricher -> enricher.enrich(record, request));
        sink.accept(record);
    }

    private boolean isExcluded(String path) {
        for (String pattern : properties.getExcludePatterns()) {
            if (pattern.startsWith("*.")) {
                if (pathMatcher.match(pattern, path)) {
                    return true;
                }
            } else if (pattern.startsWith(".")) {
                if (path.endsWith(pattern)) {
                    return true;
                }
            } else if (path.startsWith(pattern) || pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        if (!properties.isIncludeRequestPayload() || request instanceof RequestLogRequestWrapper) {
            return request;
        }
        String contentType = request.getContentType();
        if (contentType == null || request.getContentLengthLong() == 0) {
            return request;
        }
        String lower = contentType.toLowerCase(Locale.ROOT);
        boolean textual = lower.startsWith("application/json") || lower.startsWith("text/") ||
                lower.startsWith("application/x-www-form-urlencoded") || lower.contains("xml");
        return textual ? new RequestLogRequestWrapper(request) : request;
    }

    private Map<String, String[]> maskParameters(Map<String, String[]> parameters) {
        Set<String> maskKeys = maskKeys();
        Map<String, String[]> masked = new HashMap<>(parameters.size());
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            masked.put(entry.getKey(),
                    maskKeys.contains(entry.getKey().toLowerCase(Locale.ROOT))
                            ? new String[] {
                                    MASK
                            }
                            : entry.getValue());
        }
        return masked;
    }

    private @Nullable String maskPayload(@Nullable String payload) {
        if (payload == null) {
            return null;
        }
        String masked = payload;
        for (String field : properties.getMaskFields()) {
            // JSON 字段值："key":"v" / "key":123 / "key":true
            masked = Pattern
                    .compile(String.format("(\"%s\"\\s*:\\s*)(\"(?:[^\"\\\\]|\\\\.)*\"|-?[\\d.]+|true|false|null)",
                            Pattern.quote(field)), Pattern.CASE_INSENSITIVE)
                    .matcher(masked).replaceAll("$1\"" + MASK + "\"");
            // 表单字段值：key=v&
            masked = Pattern.compile(String.format("((?:^|[&?\\s])%s=)[^&\\s]*", Pattern.quote(field)),
                    Pattern.CASE_INSENSITIVE).matcher(masked).replaceAll("$1" + MASK);
        }
        return masked;
    }

    private Set<String> maskKeys() {
        Set<String> keys = new HashSet<>();
        for (String field : properties.getMaskFields()) {
            keys.add(field.toLowerCase(Locale.ROOT));
        }
        return keys;
    }

    /** TSV 单列约束：载荷里的制表/换行会破坏日志列对齐，折叠为空格。 */
    private static String sanitize(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            out.append(c == '\t' || c == '\n' || c == '\r' ? ' ' : c);
        }
        return out.toString();
    }

    private static @Nullable String realIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
