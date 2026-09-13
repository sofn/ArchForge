package com.lesofn.archforge.starter.requestlog;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * 请求日志记录模型（tab 分隔单行输出）。由
 * {@code infrastructure} 迁入 starter 化；字段顺序保持既有日志契约。
 */
public class RequestLogRecord {

    static final String SPLIT = "\t";

    private @Nullable String requestId;

    private final transient Instant date = Instant.now();

    private @Nullable String api;

    /** http method */
    private @Nullable String method;

    /** http response status */
    private int responseStatus;

    /** 来源 appkey */
    private String source = "unknow";

    private @Nullable String platform;

    private long uid;

    /** 已脱敏的 query/form 参数（掩码由过滤器按 mask-fields 配置完成） */
    private Map<String, String[]> parameters = Collections.emptyMap();

    /** 已脱敏的请求体（JSON 等可文本化载荷） */
    private @Nullable String payload;

    private @Nullable String parameterString;

    private @Nullable String response;

    private @Nullable String userAgent;

    private @Nullable String clientVersion;

    /** 用户ip,如果是内网服务器端调用，该ip是调用方通过 Api-RemoteIP机制传递的用户ip */
    private @Nullable String ip;

    /** 接口响应使用时间 */
    private long useTime;

    /** 响应大小，单位：字节 */
    private long responseSize;

    private boolean writeBody = true;

    public @Nullable String getApi() { return api; }

    public void setApi(@Nullable String api) { this.api = api; }

    public @Nullable String getMethod() { return method; }

    public void setMethod(@Nullable String method) { this.method = method; }

    public int getResponseStatus() { return responseStatus; }

    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public String getSource() { return source; }

    public void setSource(@Nullable String source) {
        if (source != null) {
            this.source = source;
        }
    }

    public long getUid() { return uid; }

    public void setUid(long uid) { this.uid = uid; }

    public @Nullable String getIp() { return ip; }

    public void setIp(@Nullable String ip) { this.ip = ip; }

    public @Nullable String getUserAgent() { return userAgent; }

    public void setUserAgent(@Nullable String userAgent) { this.userAgent = userAgent; }

    public Map<String, String[]> getParameters() { return parameters; }

    public void setParameters(Map<String, String[]> parameters) { this.parameters = parameters; }

    public @Nullable String getPayload() { return payload; }

    public void setPayload(@Nullable String payload) { this.payload = payload; }

    public long getUseTime() { return useTime; }

    public void setUseTime(long useTime) { this.useTime = useTime; }

    public @Nullable String getResponse() { return response; }

    public void setResponse(@Nullable String response) { this.response = response; }

    public @Nullable String getClientVersion() { return clientVersion; }

    public void setClientVersion(@Nullable String clientVersion) { this.clientVersion = clientVersion; }

    public @Nullable String getPlatform() { return platform; }

    public void setPlatform(@Nullable String platform) { this.platform = platform; }

    public long getResponseSize() {
        if (this.responseSize <= 0 && StringUtils.isNotBlank(this.response)) {
            this.responseSize = this.response.getBytes(UTF_8).length;
        }
        return this.responseSize;
    }

    public void setResponseSize(long responseSize) { this.responseSize = responseSize; }

    public boolean isWriteBody() { return writeBody; }

    public void setWriteBody(boolean writeBody) { this.writeBody = writeBody; }

    public @Nullable String getRequestId() { return requestId; }

    public void setRequestId(@Nullable String requestId) { this.requestId = requestId; }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.requestId);
        buf.append(SPLIT);
        buf.append(api);
        buf.append(SPLIT);
        buf.append(this.method);
        buf.append(SPLIT);
        buf.append(this.getParameterString());
        buf.append(SPLIT);
        buf.append(this.getResponseSize());
        buf.append(SPLIT);
        buf.append(this.responseStatus);
        buf.append(SPLIT);
        if (this.useTime <= 0) {
            this.useTime = ChronoUnit.MILLIS.between(this.date, Instant.now());
        }
        buf.append(this.useTime);
        buf.append(SPLIT);
        buf.append(StringUtils.isBlank(source) ? "unknow" : source);
        buf.append(SPLIT);
        buf.append(uid);
        buf.append(SPLIT);
        buf.append(this.ip);
        buf.append(SPLIT);
        buf.append(this.clientVersion != null ? this.clientVersion : "unknow");
        buf.append(SPLIT);
        buf.append(this.userAgent);
        buf.append(SPLIT);
        buf.append(this.writeBody ? this.response : "");
        return buf.toString();
    }

    private String getParameterString() {
        if (this.parameterString == null) {
            StringBuilder paramBuf = new StringBuilder();
            for (Map.Entry<String, String[]> e : this.parameters.entrySet()) {
                String key = e.getKey();
                for (String value : e.getValue()) {
                    paramBuf.append(key).append("=").append(value).append("&");
                }
            }
            if (!paramBuf.isEmpty() && paramBuf.charAt(paramBuf.length() - 1) == '&') {
                paramBuf.deleteCharAt(paramBuf.length() - 1);
            }
            if (StringUtils.isNotBlank(this.payload)) {
                if (!paramBuf.isEmpty()) {
                    paramBuf.append("&");
                }
                paramBuf.append("body=").append(this.payload);
            }
            this.parameterString = paramBuf.toString();
        }
        return parameterString;
    }
}
