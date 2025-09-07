package com.lesofn.appboot.common.encrypt

import spock.lang.Specification
import spock.lang.Unroll

import java.security.PrivateKey
import java.security.PublicKey

/**
 * RsaKeyPairGenerator单元测试
 *
 * @author sofn
 */
class RsaEncrypterTest extends Specification {

    def "should generate RSA key pair with default key size"() {
        when:
        def keyPair = RsaEncrypter.generateKeyPair()
        println keyPair.get("publicKey")
        println keyPair.get("privateKey")

        then:
        keyPair != null
        keyPair.containsKey("publicKey")
        keyPair.containsKey("privateKey")
        keyPair.get("publicKey") != null
        keyPair.get("privateKey") != null
        !keyPair.get("publicKey").isEmpty()
        !keyPair.get("privateKey").isEmpty()
    }

    def "should generate RSA key pair with specified key size"() {
        given:
        def keySize = 1024

        when:
        def keyPair = RsaEncrypter.generateKeyPair(keySize)
        println keyPair.get("publicKey")
        println keyPair.get("privateKey")

        then:
        keyPair != null
        keyPair.containsKey("publicKey")
        keyPair.containsKey("privateKey")
        keyPair.get("publicKey") != null
        keyPair.get("privateKey") != null
        !keyPair.get("publicKey").isEmpty()
        !keyPair.get("privateKey").isEmpty()
    }

    @Unroll
    def "should generate RSA key pair with different key sizes: #keySize"() {
        when:
        def keyPair = RsaEncrypter.generateKeyPair(keySize)
        println keyPair.get("publicKey")
        println keyPair.get("privateKey")

        then:
        keyPair != null
        keyPair.containsKey("publicKey")
        keyPair.containsKey("privateKey")

        where:
        keySize << [512, 1024, 2048]
    }

    def "should get PublicKey from Base64 string"() {
        given:
        def keyPair = RsaEncrypter.generateKeyPair()
        def publicKeyStr = keyPair.get("publicKey")

        when:
        PublicKey publicKey = RsaEncrypter.getPublicKey(publicKeyStr)

        then:
        publicKey != null
        publicKey.getEncoded() != null
    }

    def "should get PrivateKey from Base64 string"() {
        given:
        def keyPair = RsaEncrypter.generateKeyPair()
        def privateKeyStr = keyPair.get("privateKey")

        when:
        PrivateKey privateKey = RsaEncrypter.getPrivateKey(privateKeyStr)

        then:
        privateKey != null
        privateKey.getEncoded() != null
    }

    def "should encrypt and decrypt data correctly"() {
        given:
        def keyPair = RsaEncrypter.generateKeyPair()
        def publicKeyStr = keyPair.get("publicKey")
        def privateKeyStr = keyPair.get("privateKey")
        def originalData = "Hello, RSA Encryption!"

        when:
        def encryptedData = RsaEncrypter.encrypt(originalData, publicKeyStr)
        def decryptedData = RsaEncrypter.decrypt(encryptedData, privateKeyStr)

        then:
        encryptedData != null
        !encryptedData.isEmpty()
        encryptedData != originalData
        decryptedData == originalData
    }

    def "should encrypt and decrypt Chinese data correctly"() {
        given:
        def keyPair = RsaEncrypter.generateKeyPair()
        def publicKeyStr = keyPair.get("publicKey")
        def privateKeyStr = keyPair.get("privateKey")
        def originalData = "你好，RSA加密！"

        when:
        def encryptedData = RsaEncrypter.encrypt(originalData, publicKeyStr)
        def decryptedData = RsaEncrypter.decrypt(encryptedData, privateKeyStr)

        then:
        encryptedData != null
        !encryptedData.isEmpty()
        encryptedData != originalData
        decryptedData == originalData
    }

    def "should throw exception when decrypting with wrong private key"() {
        given:
        def keyPair1 = RsaEncrypter.generateKeyPair()
        def keyPair2 = RsaEncrypter.generateKeyPair()
        def publicKeyStr = keyPair1.get("publicKey")
        def wrongPrivateKeyStr = keyPair2.get("privateKey")
        def originalData = "Hello, RSA Encryption!"
        def encryptedData = RsaEncrypter.encrypt(originalData, publicKeyStr)

        when:
        RsaEncrypter.decrypt(encryptedData, wrongPrivateKeyStr)

        then:
        thrown(Exception)
    }

    def "should throw exception when getting public key with invalid string"() {
        given:
        def invalidPublicKeyStr = "invalid_public_key_string"

        when:
        RsaEncrypter.getPublicKey(invalidPublicKeyStr)

        then:
        thrown(Exception)
    }

    def "should throw exception when getting private key with invalid string"() {
        given:
        def invalidPrivateKeyStr = "invalid_private_key_string"

        when:
        RsaEncrypter.getPrivateKey(invalidPrivateKeyStr)

        then:
        thrown(Exception)
    }

    def "should encrypt and decrypt data correctly2"() {
        given:
        def publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCR4OIktTR8yHJpe4iirt/x8mfL0hIaePKaSCi1rKZtUWouyW9vnsSAyjerQ2xiDm9ba+syVbw57Huuw0c+SZDj24N+oMIrswkStGMmfEkwS9y/FphXoccTnqAA+DbpgoccaX9HKf3cXvge/aPy/P/mJqiJVyBrerSnsnCEpb2+SwIDAQAB"
        def privateKeyStr = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJHg4iS1NHzIcml7iKKu3/HyZ8vSEhp48ppIKLWspm1Rai7Jb2+exIDKN6tDbGIOb1tr6zJVvDnse67DRz5JkOPbg36gwiuzCRK0YyZ8STBL3L8WmFehxxOeoAD4NumChxxpf0cp/dxe+B79o/L8/+YmqIlXIGt6tKeycISlvb5LAgMBAAECgYAJ8zsJl5szvbUMG9h8huK7N+EQ5ncZ+3ufaWJEI2QbWxfGht0v0bZZNdx+iolHK5vjMk+8bbD6icuqkSL4DSrcPYfJ21NO2gA0X3ngRBYqvVgduULQmqx6bcqbQuTKddfrenOWkFTCM6Mt6HQQZJVkwO3Lh81CZiyLwTD3HK1yZQJBAMG01uI+Nq2irEYuCnb2GPPU93IWO3gxoNdIMnINGdMgsQ8tnlfwTu8zBid9vw7aYcGIHyugECfvj9FaXHkhnK8CQQDAyonTC91Gb02Vrgv6azOf1ZbxV2LGdOxpRI+9FyZrlPXTdQ9iMtKpQVkc5YbdowVfvZcyUZ+mBb54LVLjb7clAkEAsJKD/p/vf7/1gdOePmp+vGW5WgniE+JJV3S8ZzKCA+6c99UBre+kRG8igQUjEAsJaT0IDOBPWdgKJTBVcyWsiQJBAKqd1DF5K3+dGw1fNWyOGObX8LhvPJQjf+F9crPGXBMThiZ4T3/5OGPf2ExxeNSG5EehksBOajpUlQboHZX4FR0CQQCBZEnp+S/4ZC6xdbGHDYaNXZ8jeEZQTXGrjySLILXeHuaPF8i8ghARVB8FLSmbpheVSdTEoo70j1hgF5d7BHc+"
        def originalData = "admin123"
        def encryptedData1 = "J4AedoSevbizZmys5QG8Bo3xq8YI9hikDNsrbyMJ+xMidBjuK2R6vqjWBxLWf/mFxchx7kea2RiAM8S9pHCdQJymmsgSaSnVflTKgGlt/+rrJzAi0A59mHsoHLqCB6RzGwbwYSTX3MhKmtJ5SZc5hGBfp6Spad+bLHvcP2jB2hM="

        when:
        def encryptedData = RsaEncrypter.encrypt(originalData, publicKeyStr)
        println encryptedData

        def decryptedData = RsaEncrypter.decrypt(encryptedData, privateKeyStr)
        println decryptedData
        def decryptedData2 = RsaEncrypter.decrypt(encryptedData1, privateKeyStr)
        println decryptedData2

        then:
        encryptedData != null
        !encryptedData.isEmpty()
        encryptedData != originalData
        decryptedData == originalData
    }
}