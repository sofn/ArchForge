package com.lesofn.appengine.frame.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.annotation.JSONField;
import com.lesofn.appengine.common.context.ClientVersion;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sofn
 */
public class RequestContext implements Serializable, JSONAware {

    private static final long serialVersionUID = -402232948972687045L;
    @JSONField(name = "request_id")
    private String requestId;
    @JSONField(name = "current_uid")
    private long currentUid;
    private String ip;
    @JSONField(name = "app_id")
    private int appId;
    @JSONField(name = "is_official_app")
    private boolean isOfficialApp;
    @JSONField(name = "platform")
    private String platform;
    @JSONField(name = "client_version")
    private ClientVersion clientVersion;
    private Map<String, Object> attribute;

    private transient HttpServletRequest originRequest;
    // 直接从主库中读取数据
    private boolean readMasterDB;
    private boolean shouldReadMasterDB;

    public RequestContext(String requestId) {
        this.requestId = requestId;
        clientVersion = ClientVersion.NULL;
        attribute = new HashMap<>();
        readMasterDB = false;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getCurrentUid() {
        return currentUid;
    }

    public void setCurrentUid(long currentUid) {
        this.currentUid = currentUid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public ClientVersion getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(ClientVersion clientVersion) {
        if (clientVersion != null) {
            this.clientVersion = clientVersion;
        }
    }

    public boolean isOfficialApp() {
        return isOfficialApp;
    }

    public void setOfficialApp(boolean isOfficialApp) {
        this.isOfficialApp = isOfficialApp;
    }

    // 貌似是jackson的bug,transient 变量的 annotation必须加到方法上才起作用
    @JSONField(serialize = false)
    public HttpServletRequest getOriginRequest() {
        return originRequest;
    }

    public void setOriginRequest(HttpServletRequest originRequest) {
        this.originRequest = originRequest;
    }

    public Object getAttribute(String name) {
        return this.attribute.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attribute.put(name, value);
    }

    public boolean isReadMasterDB() {
        return readMasterDB;
    }

    public void setReadMasterDB(boolean readMasterDB) {
        this.readMasterDB = readMasterDB;
    }

    public boolean isShouldReadMasterDB() {
        return shouldReadMasterDB;
    }

    public void setShouldReadMasterDB(boolean shouldReadMasterDB) {
        this.shouldReadMasterDB = shouldReadMasterDB;
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

    @Override
    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
