package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求日志过滤器：打印全部请求参数（query/form/JSON body）与响应结果。
 *
 * <p>
 * 载荷捕获自包含：请求体经 {@link RequestLogRequestWrapper} eager 缓存后可重读；
 * 响应体经 {@link RequestLogResponseWrapper} tee 镜像。敏感字段按
 * {@code arch-forge.request-log.mask-fields} 脱敏为 {@code ***}：字段名（忽略大小写、下划线与连字符）
 * <b>包含</b>任一片段即命中，所以 {@code token} 能盖住 {@code accessToken} / {@code refresh_token}。
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private static final String MASK = "***";
    /** 脱敏的载荷长度下限（字符）；更大的载荷整体省略。 */
    private static final int MIN_MASK_LIMIT = 64 * 1024;
    private static final String ERROR_URI_ATTRIBUTE = "jakarta.servlet.error.request_uri";
    /** Set once a request has been recorded, so the /error dispatch that may follow does not record it again. */
    private static final String RECORDED_ATTRIBUTE = RequestLogFilter.class.getName() + ".RECORDED";
    private static final List<String> JSON_LITERALS = List.of("true", "false", "null");

    private final RequestLogProperties properties;
    private final ObjectProvider<RequestLogEnricher> enrichers;
    private final Consumer<RequestLogRecord> sink;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /** 归一化（小写、去 _ -）后的敏感片段；字段名包含任一片段即脱敏。 */
    private final Set<String> maskFragments;

    /** Proxies whose X-Forwarded-For entries are believed ({@code arch-forge.security.trusted-proxies}). */
    private final List<String> trustedProxies;

    public RequestLogFilter(RequestLogProperties properties, ObjectProvider<RequestLogEnricher> enrichers,
            Consumer<RequestLogRecord> sink) {
        this(properties, enrichers, sink, List.of());
    }

    public RequestLogFilter(RequestLogProperties properties, ObjectProvider<RequestLogEnricher> enrichers,
            Consumer<RequestLogRecord> sink, List<String> trustedProxies) {
        this.trustedProxies = List.copyOf(trustedProxies);
        this.properties = properties;
        this.enrichers = enrichers;
        this.sink = sink;
        this.maskFragments = new HashSet<>();
        // 键名归一化（小写、去 _ -）后包含任一片段即脱敏：api_key / apiKey / API-KEY / x_apikey_v2 都命中
        for (String field : properties.getMaskFields()) {
            String fragment = normalizeKey(field);
            if (!fragment.isEmpty()) {
                maskFragments.add(fragment);
            }
        }
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
        if (request.getDispatcherType() == DispatcherType.ERROR && request.getAttribute(RECORDED_ATTRIBUTE) != null) {
            filterChain.doFilter(request, response);
            return;
        }
        long startTime = System.currentTimeMillis();
        boolean completed = false;
        try {
            filterChain.doFilter(requestToUse, responseToUse);
            completed = true;
        } finally {
            // An exception escaping the chain is rendered by the /error dispatch, which records the final status;
            // recording here as well would log the request twice, the first time with the wrong status.
            if (completed || request.getDispatcherType() == DispatcherType.ERROR) {
                request.setAttribute(RECORDED_ATTRIBUTE, true);
                try {
                    writeRecord(request, requestToUse, responseToUse, path, startTime);
                } catch (RuntimeException e) {
                    // logging must never change the response the client gets
                    log.warn("request log record for {} dropped", path, e);
                }
            }
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
        Map<String, String[]> masked = new HashMap<>(parameters.size());
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            masked.put(entry.getKey(), isSensitiveKey(entry.getKey()) ? new String[] {
                    MASK
            } : entry.getValue());
        }
        return masked;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = normalizeKey(key);
        for (String fragment : maskFragments) {
            if (normalized.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    private @Nullable String maskPayload(@Nullable String payload) {
        if (payload == null) {
            return null;
        }
        // 每个敏感片段各扫一遍载荷：超大载荷既贵又无意义（默认 sink 对超长记录本就降级为摘要行），
        // 也绝不能原样放行未脱敏的内容——直接只留长度。
        int limit = Math.max(MIN_MASK_LIMIT, properties.getMaxPayloadLength());
        if (payload.length() > limit) {
            return "[" + payload.length() + " chars omitted]";
        }
        return maskForm(maskJson(payload));
    }

    /**
     * Masks the scalar value of every JSON member whose key is sensitive ({@code "key":"v"} / {@code 123} / {@code true}),
     * in one linear pass. The regex it replaces recursed once per character of a quoted value: a long value (a 30 KB
     * captcha image, an escape-heavy login body) overflowed the stack.
     */
    private String maskJson(String s) {
        int n = s.length();
        StringBuilder out = null;
        int copied = 0;
        int i = 0;
        while (i < n) {
            if (s.charAt(i) != '"') {
                i++;
                continue;
            }
            int keyEnd = stringEnd(s, i);
            int colon = skipWhitespace(s, keyEnd);
            if (colon >= n || s.charAt(colon) != ':') {
                i = keyEnd; // a string value, not a member name
                continue;
            }
            int valueStart = skipWhitespace(s, colon + 1);
            int valueEnd = isSensitiveKey(s.substring(i + 1, keyEnd - 1)) ? scalarEnd(s, valueStart) : -1;
            if (valueEnd > valueStart) {
                if (out == null) {
                    out = new StringBuilder(n);
                }
                out.append(s, copied, valueStart).append('"').append(MASK).append('"');
                copied = valueEnd;
                i = valueEnd;
            } else {
                i = valueStart;
            }
        }
        return out == null ? s : out.append(s, copied, n).toString();
    }

    /** Masks {@code key=value} pairs (form bodies, query strings) whose key is sensitive, in one linear pass. */
    private String maskForm(String s) {
        int n = s.length();
        StringBuilder out = null;
        int copied = 0;
        int i = 0;
        while (i < n) {
            boolean keyStart = i == 0 || s.charAt(i - 1) == '&' || s.charAt(i - 1) == '?' || Character.isWhitespace(s
                    .charAt(i - 1));
            int keyEnd = i;
            while (keyEnd < n && isFormKeyChar(s.charAt(keyEnd))) {
                keyEnd++;
            }
            if (!keyStart || keyEnd == i || keyEnd >= n || s.charAt(keyEnd) != '=') {
                i = Math.max(i + 1, keyEnd);
                continue;
            }
            int valueEnd = keyEnd + 1;
            while (valueEnd < n && s.charAt(valueEnd) != '&' && !Character.isWhitespace(s.charAt(valueEnd))) {
                valueEnd++;
            }
            if (isSensitiveKey(s.substring(i, keyEnd))) {
                if (out == null) {
                    out = new StringBuilder(n);
                }
                out.append(s, copied, keyEnd + 1).append(MASK);
                copied = valueEnd;
            }
            i = valueEnd;
        }
        return out == null ? s : out.append(s, copied, n).toString();
    }

    /** Index just past the closing quote of the string starting at {@code start}; the end for an unterminated one. */
    private static int stringEnd(String s, int start) {
        for (int j = start + 1; j < s.length(); j++) {
            char c = s.charAt(j);
            if (c == '\\') {
                j++;
            } else if (c == '"') {
                return j + 1;
            }
        }
        return s.length();
    }

    /** End of the JSON scalar at {@code start}, or -1 for an object, an array or nothing at all. */
    private static int scalarEnd(String s, int start) {
        if (start >= s.length()) {
            return -1;
        }
        char c = s.charAt(start);
        if (c == '"') {
            return stringEnd(s, start); // an unterminated sensitive string is masked to the end
        }
        if (c == '-' || (c >= '0' && c <= '9')) {
            int j = start + 1;
            while (j < s.length() && "0123456789.eE+-".indexOf(s.charAt(j)) >= 0) {
                j++;
            }
            return j;
        }
        for (String literal : JSON_LITERALS) {
            if (s.regionMatches(true, start, literal, 0, literal.length())) {
                return start + literal.length();
            }
        }
        return -1;
    }

    private static int skipWhitespace(String s, int start) {
        int j = start;
        while (j < s.length() && Character.isWhitespace(s.charAt(j))) {
            j++;
        }
        return j;
    }

    private static boolean isFormKeyChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '.' ||
                c == '-';
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

    /**
     * Same rule as {@code IpUtil.getClientIp} (rate limiting): forwarding headers count only when the peer is a
     * trusted proxy, and then the client is the right-most X-Forwarded-For entry that is not a trusted proxy. The
     * left-most entry is whatever the client chose to send — trusting it made the audit IP forgeable.
     */
    private @Nullable String realIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (remote == null || !trustedProxies.contains(remote)) {
            return remote;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = StringUtils.split(forwarded, ',');
            for (int i = hops.length - 1; i >= 0; i--) {
                String hop = hops[i].trim();
                if (!hop.isEmpty() && !trustedProxies.contains(hop)) {
                    return hop;
                }
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null && !realIp.isBlank() ? realIp.trim() : remote;
    }
}
