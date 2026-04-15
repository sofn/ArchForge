package com.lesofn.archsmith.common.encrypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;

/**
 * RSA密钥对生成器
 *
 * @author sofn
 */
public class RsaEncrypter {

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度，默认2048
     * @return 包含公钥和私钥的Map
     * @throws Exception 生成密钥对异常
     */
    public static Map<String, String> generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("publicKey", Base64.encodeBase64String(publicKey.getEncoded()));
        keyMap.put("privateKey", Base64.encodeBase64String(privateKey.getEncoded()));
        return keyMap;
    }

    /**
     * 生成默认长度(2048)的RSA密钥对
     *
     * @return 包含公钥和私钥的Map
     * @throws Exception 生成密钥对异常
     */
    public static Map<String, String> generateKeyPair() throws Exception {
        return generateKeyPair(2048);
    }

    /**
     * 根据Base64编码的公钥字符串获取PublicKey对象
     *
     * @param publicKeyStr Base64编码的公钥字符串
     * @return PublicKey对象
     * @throws Exception 公钥解析异常
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 根据Base64编码的私钥字符串获取PrivateKey对象
     *
     * @param privateKeyStr Base64编码的私钥字符串
     * @return PrivateKey对象
     * @throws Exception 私钥解析异常
     */
    public static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用公钥加密数据
     *
     * @param data 待加密数据
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 加密后的数据(Base64编码)
     * @throws Exception 加密异常
     */
    public static String encrypt(String data, String publicKeyStr) throws Exception {
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.encodeBase64String(encryptedData);
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedDataStr Base64编码的加密数据
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 解密后的数据
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedDataStr, String privateKeyStr) throws Exception {
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(Base64.decodeBase64(encryptedDataStr));
        return new String(decryptedData);
    }
}
