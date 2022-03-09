package com.lesofn.appengine.auth.spi;

import com.lesofn.appengine.auth.model.AuthExcepFactor;
import com.lesofn.appengine.auth.model.AuthException;
import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.common.encrypt.AESEncrypter;
import com.lesofn.appengine.common.encrypt.EncrypterException;
import com.lesofn.appengine.common.exception.EngineException;
import com.lesofn.appengine.frame.utils.log.ApiLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 */
@Component("MAuthSpi")
public class MAuthSpi extends AbstractAuthSpi {

    public static final String AUTH_HEADER_OTHER = "mauth";
    public static final String AUTH_PARAM = "mauth";
    private static final String SPI_NAME = "MAuth";
    private static final long EXPIRES_TIME = 1000 * 60 * 60 * 24 * 3;
    private static AESEncrypter encrypter = AESEncrypter.getInstance();

    public static String generateMauth(long uid) {
        return generateMauth(System.currentTimeMillis(), uid);
    }

    public static String generateMauth(long time, long uid) {
        return encrypter.encrypt(time + ":" + uid);
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        String authHeader = StringUtils.isBlank(request.getHeader(AUTH_HEADER)) ? request.getHeader(AUTH_HEADER_OTHER) : request.getHeader(AUTH_HEADER);
        if (!StringUtils.isBlank(authHeader) && authHeader.toLowerCase().startsWith(SPI_NAME.toLowerCase() + " ")) {
            if (ApiLogger.isDebugEnabled()) {
                ApiLogger.debug("find mauth parameter in header:" + authHeader);
            }
            return true;
        }
        String param_auth = request.getParameter(AUTH_PARAM);
        if (StringUtils.isNotBlank(param_auth) && StringUtils.startsWithIgnoreCase(param_auth, SPI_NAME + "") && param_auth.length() == 70) {
            if (ApiLogger.isDebugEnabled()) {
                ApiLogger.debug("find mauth parameter in param:" + authHeader);
            }
            return true;
        }
        return false;
    }

    @Override
    public long auth(AuthRequest request) throws AuthException {
        String authHeader = StringUtils.isBlank(request.getHeader(AUTH_HEADER)) ? request.getHeader(AUTH_HEADER_OTHER) : request.getHeader(AUTH_HEADER);
        if (StringUtils.isBlank(authHeader)) {
            authHeader = request.getParameter(AUTH_PARAM);
        }

        String[] ss = authHeader.split(" ");
        if (ss.length != 2) {
            ApiLogger.error("Authorization header error, authHeader:" + authHeader);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }
        String aesHeader = ss[1];
        try {
            String decryptedString = encrypter.decryptAsString(aesHeader);
            String[] timeAndUid = decryptedString.split(":");
            long time = NumberUtils.toLong(timeAndUid[0], 0);
            long now = System.currentTimeMillis();
            if (now - time > EXPIRES_TIME) {
                throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL, "token expires.");
            }
            long uid = NumberUtils.toLong(timeAndUid[1], 0);
            if (uid <= 0) {
                throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL, "invalid uid.");
            }
            return uid;
        } catch (EngineException e) {
            ApiLogger.error("mauth " + e.getMessage() + ", authHeader:" + authHeader);
            throw e;
        } catch (EncrypterException e) {
            ApiLogger.error("auth decrypt mauth token error,header:" + authHeader);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        } catch (RuntimeException e) {
            ApiLogger.error("auth fail mauth,header:" + authHeader, e);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }
    }
}
