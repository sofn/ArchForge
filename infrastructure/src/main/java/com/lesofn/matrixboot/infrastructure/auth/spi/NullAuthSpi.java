package com.lesofn.matrixboot.infrastructure.auth.spi;

import com.lesofn.matrixboot.infrastructure.auth.model.AuthExcepFactor;
import com.lesofn.matrixboot.infrastructure.auth.model.AuthException;
import com.lesofn.matrixboot.infrastructure.auth.model.AuthRequest;
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
    public long auth(AuthRequest request) throws AuthException {
        throw new AuthException(AuthExcepFactor.E_USER_AUTHFAIL, "NullAuthSpi::doAuth");
    }

}
