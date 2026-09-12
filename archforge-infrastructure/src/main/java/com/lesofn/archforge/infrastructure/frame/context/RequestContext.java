package com.lesofn.archforge.infrastructure.frame.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lesofn.archforge.common.context.ClientVersion;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * 请求上下文载体。
 *
 * @author sofn
 */
public class RequestContext implements Serializable {

    @JsonProperty("request_id")
    private @Nullable String requestId;

    @JsonProperty("current_uid")
    private long currentUid;

    private @Nullable String ip;

    @JsonProperty("app_id")
    private int appId;

    @JsonProperty("is_official_app")
    private boolean isOfficialApp;

    @JsonProperty("platform")
    private @Nullable String platform;

    @JsonProperty("client_version")
    private ClientVersion clientVersion;

    private @Nullable Map<String, @Nullable Object> attribute;

    private transient @Nullable HttpServletRequest originRequest;

    public RequestContext(String requestId) {
        this.requestId = requestId;
        clientVersion = ClientVersion.NULL;
        attribute = new HashMap<>();
    }

    public @Nullable String getRequestId() { return requestId; }

    public void setRequestId(@Nullable String requestId) { this.requestId = requestId; }

    public long getCurrentUid() { return currentUid; }

    public void setCurrentUid(long currentUid) { this.currentUid = currentUid; }

    public @Nullable String getIp() { return ip; }

    public void setIp(@Nullable String ip) { this.ip = ip; }

    public int getAppId() { return appId; }

    public void setAppId(int appId) { this.appId = appId; }

    public @Nullable ClientVersion getClientVersion() { return clientVersion; }

    public void setClientVersion(@Nullable ClientVersion clientVersion) {
        if (clientVersion != null) {
            this.clientVersion = clientVersion;
        }
    }

    public boolean isOfficialApp() { return isOfficialApp; }

    public void setOfficialApp(boolean isOfficialApp) { this.isOfficialApp = isOfficialApp; }

    // 貌似是jackson的bug,transient 变量的 annotation必须加到方法上才起作用
    @JsonIgnore
    public @Nullable HttpServletRequest getOriginRequest() { return originRequest; }

    public void setOriginRequest(@Nullable HttpServletRequest originRequest) { this.originRequest = originRequest; }

    public @Nullable Object getAttribute(String name) {
        Map<String, @Nullable Object> attrs = this.attribute;
        return attrs == null ? null : attrs.get(name);
    }

    public void setAttribute(String name, @Nullable Object value) {
        Map<String, @Nullable Object> attrs = this.attribute;
        if (attrs == null) {
            attrs = new HashMap<>();
            this.attribute = attrs;
        }
        attrs.put(name, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + appId;
        result = prime * result + ((clientVersion == null) ? 0 : clientVersion.hashCode());
        result = prime * result + (int) (currentUid ^ (currentUid >>> 32));
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("EqualsGetClass") // 请求上下文按精确类型判等
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestContext other = (RequestContext) obj;
        if (appId != other.appId)
            return false;
        if (clientVersion == null) {
            if (other.clientVersion != null)
                return false;
        } else if (!clientVersion.equals(other.clientVersion))
            return false;
        if (currentUid != other.currentUid)
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (requestId == null) {
            if (other.requestId != null)
                return false;
        } else if (!requestId.equals(other.requestId))
            return false;
        return true;
    }

    public String toJSONString() {
        return JsonUtil.to(this);
    }

    public @Nullable String getPlatform() { return platform; }

    public void setPlatform(@Nullable String platform) { this.platform = platform; }
}
