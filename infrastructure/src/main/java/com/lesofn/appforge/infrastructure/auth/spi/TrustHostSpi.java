package com.lesofn.appforge.infrastructure.auth.spi;

import com.lesofn.appforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.appforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.appforge.infrastructure.auth.model.AuthRequest;
import com.lesofn.appforge.infrastructure.auth.service.AuthService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 */
@Component("TrustHostSpi")
public class TrustHostSpi extends AbstractAuthSpi {

    public static final String SPI_NAME = "TrustHost";

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        return request.getFrom() == AuthRequest.RequestFrom.INNER
                && request.getHeader(AuthService.ENGINE_UID_HEADER) != null;
    }

    @Override
    public long auth(AuthRequest request) throws AdminAuthException {
        long uid = NumberUtils.toLong(request.getHeader(AuthService.ENGINE_UID_HEADER), 0);
        if (uid == 0) {
            throw new AdminAuthException(
                    AdminAuthErrorCode.USER_AUTHFAIL, "Engine uid header is empty.");
        }
        return uid;
    }
}
