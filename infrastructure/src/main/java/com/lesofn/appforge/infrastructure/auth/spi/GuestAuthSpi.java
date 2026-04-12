package com.lesofn.appforge.infrastructure.auth.spi;

import com.lesofn.appforge.common.context.ClientVersion;
import com.lesofn.appforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.appforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.appforge.infrastructure.auth.model.AuthRequest;
import org.springframework.stereotype.Component;

/**
 * 验证Header基本信息，暂时只验证版本号不为空
 *
 * @author sofn 10/03/15.
 */
@Component("guestSpi")
public class GuestAuthSpi extends AbstractAuthSpi {

    public static final String SPI_NAME = "GUESS_AUTH";

    @Override
    protected boolean checkCanAuth(AuthRequest request) {
        //此spi需手动指定
        return false;
    }

    @Override
    public long auth(AuthRequest request) throws AdminAuthException {
        ClientVersion version = ClientVersion.valueOf(request.getHeader(ClientVersion.VERSION_HEADER));
        if ((version.sdkVersion.equals(ClientVersion.Version.NULL)
                || version.clientVersion.equals(ClientVersion.Version.NULL)
                || version.udid.equals(ClientVersion.DEFAULT_UNKNOW))
                && request.getFrom() != AuthRequest.RequestFrom.INNER) {
            throw new AdminAuthException(AdminAuthErrorCode.ILLEGAL_GUEST);
        }
        return 0;
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }
}
