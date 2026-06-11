package com.lesofn.archforge.infrastructure.auth.spi;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.AuthRequest;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 */
@Component("NullSpi")
public class NullAuthSpi extends AbstractAuthSpi {

    public static final String SPI_NAME = "Null";

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        return true;
    }

    @Override
    public long auth(AuthRequest request) throws AdminAuthException {
        throw new AdminAuthException(AdminAuthErrorCode.USER_AUTHFAIL, "NullAuthSpi::doAuth");
    }
}
