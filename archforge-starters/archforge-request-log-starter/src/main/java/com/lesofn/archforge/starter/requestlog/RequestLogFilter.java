package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
 * {@code arch-forge.request-log.mask-fields} 脱敏为 {@code ***}：字段名（忽略大小写、下划线与连字符）
 * <b>包含</b>任一片段即命中，所以 {@code token} 能盖住 {@code accessToken} / {@code refresh_token}。
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private static final String MASK = "***";
    /** 脱敏的载荷长度下限（字符）；更大的载荷整体省略。 */
    private static final int MIN_MASK_LIMIT = 64 * 1024;
    private static final String ERROR_URI_ATTRIBUTE = "jakarta.servlet.error.request_uri";

    private final RequestLogProperties properties;
    private final ObjectProvider<RequestLogEnricher> enrichers;
    private final Consumer<RequestLogRecord> sink;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /** 归一化（小写、去 _ -）后的敏感片段；字段名包含任一片段即脱敏。 */
    private final Set<String> maskFragments;
    private final List<Pattern> jsonMaskPatterns = new ArrayList<>();
    private final List<Pattern> formMaskPatterns = new ArrayList<>();

    public RequestLogFilter(RequestLogProperties properties, ObjectProvider<RequestLogEnricher> enrichers,
            Consumer<RequestLogRecord> sink) {
        this.properties = properties;
        this.enrichers = enrichers;
        this.sink = sink;
        this.maskFragments = new HashSet<>();
        for (String field : properties.getMaskFields()) {
            String fragment = normalizeKey(field);
            if (fragment.isEmpty() || !maskFragments.add(fragment)) {
                continue;
            }
            // 片段的相邻字符间允许出现 _ 或 -，且前后可带任意键名字符：api_key / apiKey / API-KEY / x_apikey_v2 都命中
            String keyPattern = fragment.chars().mapToObj(c -> Pattern.quote(String.valueOf((char) c)))
                    .collect(Collectors.joining("[_-]*"));
            jsonMaskPatterns.add(Pattern.compile(
                    "(\"[^\"\\\\]*" + keyPattern + "[^\"\\\\]*\"\\s*:\\s*)(\"(?:[^\"\\\\]|\\\\.)*\"|-?[\\d.]+|true|false|null)",
                    Pattern.CASE_INSENSITIVE));
            formMaskPatterns.add(Pattern.compile("((?:^|[&?\\s])[\\w.\\-]*" + keyPattern + "[\\w.\\-]*=)[^&\\s]*",
                    Pattern.CASE_INSENSITIVE));
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
        String masked = payload;
        for (Pattern json : jsonMaskPatterns) {
            // JSON 字段值："key":"v" / "key":123 / "key":true
            masked = json.matcher(masked).replaceAll("$1\"" + MASK + "\"");
        }
        for (Pattern form : formMaskPatterns) {
            // 表单字段值：key=v&
            masked = form.matcher(masked).replaceAll("$1" + MASK);
        }
        return masked;
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
