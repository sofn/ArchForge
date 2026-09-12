package com.lesofn.archforge.infrastructure.auth.spi;

import com.google.common.base.Splitter;
import com.lesofn.archforge.common.auth.AuthRequest;
import com.lesofn.archforge.common.encrypt.AESEncrypter;
import com.lesofn.archforge.common.encrypt.EncrypterException;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.frame.utils.log.ApiLogger;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

/**
 * MAuth 认证 SPI 实现。
 *
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
    public String getName() { return SPI_NAME; }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        String authHeader = StringUtils.isBlank(request.getHeader(AUTH_HEADER))
                ? request.getHeader(AUTH_HEADER_OTHER)
                : request.getHeader(AUTH_HEADER);
        if (!StringUtils.isBlank(authHeader) && authHeader.toLowerCase(Locale.ROOT).startsWith(SPI_NAME.toLowerCase(
                Locale.ROOT) + " ")) {
            if (ApiLogger.isDebugEnabled()) {
                ApiLogger.debug("find mauth parameter in header:" + authHeader);
            }
            return true;
        }
        String paramAuth = request.getParameter(AUTH_PARAM);
        if (StringUtils.isNotBlank(paramAuth) && Strings.CI.startsWith(paramAuth, SPI_NAME + "") && paramAuth
                .length() == 70) {
            if (ApiLogger.isDebugEnabled()) {
                ApiLogger.debug("find mauth parameter in param:" + authHeader);
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("StringSplitter") // "token "（尾空格）必须判 size!=2 失败，split() 丢弃尾空段正是所需语义
    public long auth(AuthRequest request) throws AdminAuthException {
        String authHeader = StringUtils.isBlank(request.getHeader(AUTH_HEADER))
                ? request.getHeader(AUTH_HEADER_OTHER)
                : request.getHeader(AUTH_HEADER);
        if (StringUtils.isBlank(authHeader)) {
            authHeader = request.getParameter(AUTH_PARAM);
        }

        String[] ss = authHeader.split(" ");
        if (ss.length != 2) {
            ApiLogger.error("Authorization header error, authHeader:" + authHeader);
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }
        String aesHeader = ss[1];
        try {
            String decryptedString = encrypter.decryptAsString(aesHeader);
            List<String> timeAndUid = Splitter.on(':').splitToList(decryptedString);
            long time = NumberUtils.toLong(timeAndUid.get(0), 0);
            long now = System.currentTimeMillis();
            if (now - time > EXPIRES_TIME) {
                throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL, "token expires.");
            }
            long uid = NumberUtils.toLong(timeAndUid.get(1), 0);
            if (uid <= 0) {
                throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL, "invalid uid.");
            }
            return uid;
        } catch (AdminAuthException e) {
            ApiLogger.error("mauth " + e.getMessage() + ", authHeader:" + authHeader);
            throw e;
        } catch (EncrypterException e) {
            ApiLogger.error("auth decrypt mauth token error,header:" + authHeader);
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        } catch (RuntimeException e) {
            ApiLogger.error("auth fail mauth,header:" + authHeader, e);
            throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL);
        }
    }
}
