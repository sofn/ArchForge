package com.lesofn.appengine.auth.spi;

import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.frame.utils.log.ApiLogger;

/**
 * @author sofn
 */
public abstract class AbstractAuthSpi implements AuthSpi {

    @Override
    public boolean canAuth(AuthRequest request) {
        try {
            boolean result = this.checkCanAuth(request);
            if (ApiLogger.isTraceEnabled()) {
                ApiLogger.trace(this.getName() + " can auth:" + result);
            }
            return result;
        } catch (Exception e) {
            ApiLogger.error("check can auth error:", e);
            return false;
        }
    }

    protected abstract boolean checkCanAuth(AuthRequest request);

    @Override
    public void afterAuth(long uid, AuthRequest request) {
        // default do nothing
    }
}
