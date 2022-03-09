package com.lesofn.appengine.auth.service;

import com.lesofn.appengine.auth.annotation.BaseInfo;
import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.auth.model.AuthResponse;

import java.util.Optional;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-8-30 00:22.
 */
public interface AuthService {
    String ENGINE_REMOTEIP_HEADER = "X-Engine-RemoteIP";
    String ENGINE_UID_HEADER = "X-Engine-UID";
    String ENGINE_APPID_HEADER = "X-Engine-APPID";

    AuthResponse auth(AuthRequest request, Optional<BaseInfo> baseInfo);
}
