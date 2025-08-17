package com.lesofn.matrixboot.auth.spi;

import com.lesofn.matrixboot.auth.model.AuthExcepFactor;
import com.lesofn.matrixboot.auth.model.AuthException;
import com.lesofn.matrixboot.auth.model.AuthRequest;
import com.lesofn.matrixboot.common.encrypt.AESEncrypter;
import com.lesofn.matrixboot.common.encrypt.EncrypterException;
import com.lesofn.matrixboot.common.exception.EngineException;
import com.lesofn.matrixboot.frame.utils.log.ApiLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author sofn 11/24/14.
 */
@Component("CookieAuthSpi")
public class CookieAuthSpi extends AbstractAuthSpi {

    private static final String SPI_NAME = "CAuth";
    public static final long COOKIE_EXPIRES_TIME = TimeUnit.HOURS.toSeconds(3);
    private static final long EXPIRES_TIME = 1000 * COOKIE_EXPIRES_TIME;
    private static AESEncrypter encrypter = AESEncrypter.getInstance();

    public static String generateCookie(long uid) {
        return generateCookie(System.currentTimeMillis(), uid);
    }

    public static String generateCookie(long time, long uid) {
        return encrypter.encrypt(time + ":" + uid);
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        String cookie = request.getCookie(COOKIE_NAME);
        if (StringUtils.isNotBlank(cookie)) {
            if (ApiLogger.isDebugEnabled()) {
                ApiLogger.debug("find cauth parameter in cookie:" + cookie);
            }
            return true;
        }
        return false;
    }

    @Override
    public long auth(AuthRequest request) throws AuthException {
        String cookie = request.getCookie(COOKIE_NAME);
        try {
            String decryptedString = encrypter.decryptAsString(cookie);
            String[] timeAndUid = decryptedString.split(":");
            long time = NumberUtils.toLong(timeAndUid[0], 0);
            long now = System.currentTimeMillis();
            if (time > now || now - time > EXPIRES_TIME) {
                throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL, "cookie expires.");
            }
            long uid = NumberUtils.toLong(timeAndUid[1], 0);
            if (uid <= 0) {
                throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL, "invalid uid.");
            }
            return uid;
        } catch (EngineException e) {
            ApiLogger.error("cauth " + e.getMessage() + ", cookieStr:" + cookie);
            throw e;
        } catch (EncrypterException e) {
            ApiLogger.error("cauth decrypt auth token error, cookieStr:" + cookie);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        } catch (RuntimeException e) {
            ApiLogger.error("cauth fail auth, cookieStr:" + cookie, e);
            throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL);
        }
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }
}
