package com.lesofn.archforge.common.encrypt;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import org.jspecify.annotations.Nullable;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * AES工具类，提供加密解密、生成Key等方法
 *
 * @author sofn
 */
public class AESEncrypter {

    /** dedicated monitor — never lock on a String constant (interned/shared) */
    private static final Object LOCK = new Object();

    private static final String AESKEYSTR = "ZTZjZGVjZDcxNmMwMWQzZTIzOWE4ZjNkZjk3ZTJiZTM=";

    private SecretKey aesKey;

    private AESEncrypter() {
        aesKey = loadAesKey();
    }

    private AESEncrypter(String aes) {
        aesKey = loadAesKey(aes);
    }

    private static volatile @Nullable AESEncrypter INSTANCE;

    private static final Map<String, AESEncrypter> INSTANCES = new ConcurrentHashMap<>();

    public static AESEncrypter getInstance() {
        AESEncrypter instance = INSTANCE;
        if (instance == null) {
            synchronized (LOCK) {
                instance = INSTANCE;
                if (instance == null) {
                    instance = new AESEncrypter();
                    INSTANCE = instance;
                }
            }
        }
        return instance;
    }

    public static AESEncrypter getInstance(String aes) {
        return INSTANCES.computeIfAbsent(aes, AESEncrypter::new);
    }

    public String encrypt(String msg) {
        try {
            Cipher ecipher = Cipher.getInstance("AES");
            ecipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return Hex.encodeHexString(ecipher.doFinal(msg.getBytes()));
        } catch (Exception e) {
            String errMsg = "decrypt error, data:" + msg;
            throw new EncrypterException(errMsg, e);
        }
    }

    public byte[] decrypt(String msg) {
        try {
            Cipher dcipher = Cipher.getInstance("AES");
            dcipher.init(Cipher.DECRYPT_MODE, aesKey);
            return dcipher.doFinal(Hex.decodeHex(msg.toCharArray()));
        } catch (Exception e) {
            String errMsg = "decrypt error, data:" + msg;
            throw new EncrypterException(errMsg, e);
        }
    }

    public String decryptAsString(String msg) {
        return new String(this.decrypt(msg));
    }

    private static SecretKey loadAesKey() {
        return loadAesKey(AESKEYSTR);
    }

    private static SecretKey loadAesKey(String aesKeyStr) {
        String buffer = new String(Base64.decodeBase64(aesKeyStr));
        byte[] keyStr;
        try {
            keyStr = Hex.decodeHex(buffer.toCharArray());
        } catch (DecoderException e) {
            throw new EncrypterException(e);
        }
        return new SecretKeySpec(keyStr, "AES");
    }

    private static String generateAesKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            // 产生密钥
            SecretKey secretKey = keyGenerator.generateKey();
            // 获取密钥
            byte[] keyBytes = secretKey.getEncoded();
            return Base64.encodeBase64String(Hex.encodeHexString(keyBytes).getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new EncrypterException(e);
        }
    }
}
