package com.lesofn.archforge.infrastructure.frame.utils;

import com.lesofn.archforge.common.context.ClientVersion;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import tools.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-06-26 12:19
 */
@SuppressWarnings("NullAway.Init")
public class RequestLogRecord {
    static final String SPLIT = "\t";

    private @Nullable String requestId;

    private final transient java.time.Instant date = java.time.Instant.now();

    private @Nullable String api;

    /** http method */
    private @Nullable String method;

    /** http response status */
    private int responseStatus;

    /** 来源 appkey */
    private String source = "unknow";

    private @Nullable String platform;

    private long uid;

    private Map<String, String[]> parameters = Collections.emptyMap();

    private @Nullable String parameterString;

    private @Nullable String response;

    private @Nullable String userAgent;

    private @Nullable ClientVersion clientVersion = ClientVersion.NULL;

    // /**
    // * 调用方ip，如果是 内网服务端调用，该ip是服务器ip， 否则该ip与用户ip一致
    // */
    // private String clientIp;

    /** 用户ip,如果是内网服务器端调用，该ip是调用方通过 Api-RemoteIP机制传递的用户ip */
    private @Nullable String ip;

    /** 接口响应使用时间 */
    private long useTime;

    /** 响应大小，单位：字节 */
    private long responseSize;

    private boolean writeBody = true;

    public RequestLogRecord() {
    }

    public @Nullable String getApi() { return api; }

    public void setApi(@Nullable String api) { this.api = api; }

    public @Nullable String getMethod() { return method; }

    public void setMethod(@Nullable String method) { this.method = method; }

    public int getResponseStatus() { return responseStatus; }

    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public String getSource() { return source; }

    public void setSource(String source) {
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

    public void setParameterString(@Nullable String parameterString) { this.parameterString = parameterString; }

    public long getUseTime() { return useTime; }

    public void setUseTime(long useTime) { this.useTime = useTime; }

    public @Nullable String getResponse() { return response; }

    public void setResponse(@Nullable String response) { this.response = response; }

    public @Nullable ClientVersion getClientVersion() { return clientVersion; }

    public void setClientVersion(@Nullable ClientVersion clientVersion) {
        if (clientVersion != null) {
            this.clientVersion = clientVersion;
        }
    }

    public long getResponseSize() {
        if (this.responseSize <= 0 && StringUtils.isNotBlank(this.response)) {
            this.responseSize = this.response.getBytes().length;
        }
        return this.responseSize;
    }

    public void setResponseSize(long responseSize) { this.responseSize = responseSize; }

    public boolean isWriteBody() { return writeBody; }

    public void setWriteBody(boolean writeBody) { this.writeBody = writeBody; }

    public ObjectNode toJSONObject() {
        return JsonUtil.getObjectMapper().createObjectNode();
    }

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
            this.useTime = java.time.temporal.ChronoUnit.MILLIS.between(this.date, java.time.Instant.now());
        }
        buf.append(this.useTime);
        buf.append(SPLIT);
        buf.append(StringUtils.isBlank(source) ? "unknow" : source);
        buf.append(SPLIT);
        buf.append(uid);
        buf.append(SPLIT);
        buf.append(this.ip);
        buf.append(SPLIT);
        if (this.clientVersion != null) {
            buf.append(this.clientVersion);
        } else {
            buf.append(ClientVersion.NULL);
        }
        buf.append(SPLIT);
        buf.append(this.userAgent);
        buf.append(SPLIT);
        buf.append(this.writeBody ? this.response : "");
        return buf.toString();
    }

    public String toJsonString() {
        return JsonUtil.to(this);
    }

    private String getParameterString() {
        if (this.parameterString == null) {
            if (this.parameters != null) {
                StringBuilder paramBuf = new StringBuilder();
                for (Map.Entry<String, String[]> e : this.parameters.entrySet()) {
                    String key = e.getKey();
                    String[] values = e.getValue();
                    for (String value : values) {
                        value = passwordEscape(key, value);
                        paramBuf.append(key).append("=").append(value);
                        paramBuf.append("&");
                    }
                }
                if (paramBuf.length() > 0 && paramBuf.charAt(paramBuf.length() - 1) == '&') {
                    paramBuf.deleteCharAt(paramBuf.length() - 1);
                }
                this.parameterString = paramBuf.toString();
            } else {
                this.parameterString = "";
            }
        }
        return parameterString;
    }

    private String passwordEscape(String key, String value) {
        if (Strings.CS.equals("password", key) || Strings.CS.equals("old_password", key)) {
            return "***";
        }
        return value;
    }

    public @Nullable String getRequestId() { return requestId; }

    public void setRequestId(@Nullable String requestId) { this.requestId = requestId; }

    public @Nullable String getPlatform() { return platform; }

    public void setPlatform(@Nullable String platform) { this.platform = platform; }
}
