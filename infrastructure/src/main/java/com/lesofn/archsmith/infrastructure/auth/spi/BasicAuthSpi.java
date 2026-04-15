package com.lesofn.archsmith.infrastructure.auth.spi;

import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.auth.model.AuthRequest;
import com.lesofn.archsmith.infrastructure.auth.provider.UserProvider;
import com.lesofn.archsmith.infrastructure.auth.service.DefaultAuthService;
import com.lesofn.archsmith.infrastructure.frame.utils.log.ApiLogger;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

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
        return "Basic "
                + new String(Base64.encodeBase64((username + ":" + password).getBytes(), false));
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        String authString = request.getHeader(AUTH_HEADER);
        return Strings.CS.startsWith(authString, "Basic");
    }

    @Override
    public long auth(AuthRequest request) {
        String authString = request.getHeader("Authorization");
        if (StringUtils.isBlank(authString)) {
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }
        if (ApiLogger.isTraceEnabled()) {
            ApiLogger.trace("basic auth string:" + authString);
        }
        String base64 = authString.substring(6);
        if (StringUtils.isBlank(base64)) {
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }
        String nameAndPasswd = new String(Base64.decodeBase64(base64.getBytes()));

        int pos = nameAndPasswd.indexOf(":");
        if (pos < 1 || pos == (nameAndPasswd.length() - 1)) {
            ApiLogger.warn("403 PWD error, error user pass: " + nameAndPasswd);
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }

        String username = nameAndPasswd.substring(0, pos);
        String password = nameAndPasswd.substring(pos + 1);
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }

        long uid = 0;
        Optional<UserProvider> provider = DefaultAuthService.getUserProvider();
        if (provider.isPresent()) {
            uid = provider.get().authUser(username, password);
        }
        if (uid <= 0) {
            throw new AdminAuthException(
                    AdminAuthErrorCode.USERNAME_PASSWORD_ERROR, "username or password error");
        }
        return uid;
    }
}
