package com.lesofn.archforge.server.admin.dto;

import com.lesofn.archforge.common.enums.dictionary.DictionaryData;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import lombok.NoArgsConstructor;

/**
 * 系统配置 DTO
 *
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class LoginConfigResponse {

    /** 是否开启验证码 */
    private Boolean isCaptchaOn = true;

    private Map<String, List<DictionaryData>> dictionary;

    /**
     * RSA public key (X.509 SPKI, Base64) to encrypt the login password with: RSA-OAEP with SHA-256, as in WebCrypto.
     * Null when the server has no private key configured; the client then sends the password as typed.
     */
    private @Nullable String rsaPublicKey;
}
