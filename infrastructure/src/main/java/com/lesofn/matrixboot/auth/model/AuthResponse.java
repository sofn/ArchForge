package com.lesofn.matrixboot.auth.model;

import com.lesofn.matrixboot.common.context.ClientVersion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-09-17 22:06.
 */
public class AuthResponse implements Serializable {
    private static final long serialVersionUID = 5453139780159342985L;

    private final long uid;
    private final Map<String, Object> attributes;
    private final String ip;
    private final int appId;
    private final String authedBy;
    private final String platform;
    private final ClientVersion clientVersion;

    public AuthResponse(String platform, long uid, String ip, int appId, String authedBy, ClientVersion clientVersion) {
        this.platform = platform;
        this.uid = uid;
        this.attributes = new HashMap<>();
        this.ip = ip;
        this.appId = appId;
        this.authedBy = authedBy;
        this.clientVersion = clientVersion;
    }

    public long getUid() {
        return uid;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Set<Map.Entry<String, Object>> getAttributes() {
        return attributes.entrySet();
    }

    public String getIp() {
        return ip;
    }

    public int getAppId() {
        return appId;
    }

    public String getAuthedBy() {
        return authedBy;
    }

    public String getPlatform() {
        return platform;
    }

    public ClientVersion getClientVersion() {
        return clientVersion;
    }
}
