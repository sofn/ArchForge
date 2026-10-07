package com.lesofn.archforge.common.encrypt;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * AES-GCM 工具：认证加密，每条消息随机 IV。
 *
 * <p>
 * 没有内置密钥——密钥必须由调用方从配置里显式传入（{@code archforge init} 生成的 {@code AES_KEY}：
 * 16/24/32 字节的标准 Base64）。密文格式为 hex({@code IV(12) || 密文 || 认证标签(16)})。
 * 所有异常消息都不包含明文、密文或密钥。
 *
 * @author sofn
 */
public final class AESEncrypter {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey key;

    private AESEncrypter(SecretKey key) {
        this.key = key;
    }

    /**
     * 以 Base64 编码的 AES 密钥创建实例。
     *
     * @param base64Key 16/24/32 字节密钥的标准 Base64
     * @throws EncrypterException 不是 Base64 或长度不是合法的 AES 密钥长度（消息不回显密钥）
     */
    public static AESEncrypter of(String base64Key) {
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new EncrypterException("AES key is not valid Base64");
        }
        if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
            throw new EncrypterException("AES key must be 16, 24 or 32 bytes, got " + raw.length);
        }
        return new AESEncrypter(new SecretKeySpec(raw, "AES"));
    }

    public String encrypt(String msg) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(msg.getBytes(UTF_8));
            byte[] out = new byte[IV_BYTES + encrypted.length];
            System.arraycopy(iv, 0, out, 0, IV_BYTES);
            System.arraycopy(encrypted, 0, out, IV_BYTES, encrypted.length);
            return Hex.encodeHexString(out);
        } catch (GeneralSecurityException e) {
            throw new EncrypterException("AES encrypt failed", e);
        }
    }

    public byte[] decrypt(String msg) {
        try {
            byte[] in = Hex.decodeHex(msg.toCharArray());
            if (in.length < IV_BYTES + TAG_BITS / 8) {
                throw new EncrypterException("AES decrypt failed: ciphertext too short");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, in, 0, IV_BYTES));
            return cipher.doFinal(in, IV_BYTES, in.length - IV_BYTES);
        } catch (DecoderException | GeneralSecurityException e) {
            // 不带入 e 的消息：DecoderException 会复述非法输入
            throw new EncrypterException("AES decrypt failed: malformed or tampered ciphertext");
        }
    }

    public String decryptAsString(String msg) {
        return new String(decrypt(msg), UTF_8);
    }
}
