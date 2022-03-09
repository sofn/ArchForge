package com.lesofn.appengine.auth.spi;

import com.lesofn.appengine.auth.model.AuthExcepFactor;
import com.lesofn.appengine.auth.model.AuthException;
import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.auth.provider.UserProvider;
import com.lesofn.appengine.auth.service.DefaultAuthService;
import com.lesofn.appengine.frame.utils.log.ApiLogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author sofn
 */
@Component("BasicAuthSpi")
public class BasicAuthSpi extends AbstractAuthSpi {

    public static final String SPI_NAME = "basic";

    @Override
    public String getName() {
        return SPI_NAME;
    }

    public static String generateBasicAuthHeader(String username, String password) {
        return "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes(), false));
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        String authString = request.getHeader(AUTH_HEADER);
        return StringUtils.startsWith(authString, "Basic");
    }

    @Override
    public long auth(AuthRequest request) {
        String authString = request.getHeader("Authorization");
        if (StringUtils.isBlank(authString)) {
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }
        if (ApiLogger.isTraceEnabled()) {
            ApiLogger.trace("basic auth string:" + authString);
        }
        String base64 = authString.substring(6);
        if (StringUtils.isBlank(base64)) {
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }
        String nameAndPasswd = new String(
                Base64.decodeBase64(base64.getBytes()));

        int pos = nameAndPasswd.indexOf(":");
        if (pos < 1 || pos == (nameAndPasswd.length() - 1)) {
            ApiLogger.warn("403 PWD error, error user pass: " + nameAndPasswd);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }

        String username = nameAndPasswd.substring(0, pos);
        String password = nameAndPasswd.substring(pos + 1);
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }

        long uid = 0;
        Optional<UserProvider> provider = DefaultAuthService.getUserProvider();
        if (provider.isPresent()) {
            uid = provider.get().authUser(username, password);
        }
        if (uid <= 0) {
            throw new AuthException(AuthExcepFactor.E_AUTH_PASSWORD_ERROR, "username or password error");
        }
        return uid;
    }

}
