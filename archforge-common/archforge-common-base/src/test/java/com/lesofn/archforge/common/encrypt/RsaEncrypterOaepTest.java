package com.lesofn.archforge.common.encrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The login password is encrypted by the admin UI with the browser's WebCrypto RSA-OAEP (SHA-256, MGF1-SHA-256);
 * the server must decrypt exactly that. The fixture below was produced by Node's WebCrypto with a throwaway key.
 */
class RsaEncrypterOaepTest {

    private static final String WEBCRYPTO_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCltQMGZjhldl54G7/4zvEgV0oLeZHet3CmCLv2QDXRdGVaGLIa" +
            "6aDnkfDAe17hxKaPro4gdBS73jAsmDOgBJNmjFvXq3u0VGubTZwfMaLVRsWJW+zBJGQsPrZhPl3Jkj5pBMDLrScYoSlq69APCSY8" +
            "pXZw27DP1XnOq2yv16BnOe+dfNa1xMv+rukzaeT++0R/JbVSWioatgAq5sUXtORAO6Blg+fn0D2whrUHAau7BtpVyjfHUrC3WyQo" +
            "1VbU5ycz1zk9JMhf0k7ReVWbxQfqTYSlnIoekHJL7Mb2TIUZ3OTZiPeLQ+YTbygOkLaS93cm3Mqs2DauIDWreDanP+wzAgMBAAEC" +
            "ggEABUOUTOvXYJj1IRsK99qL4pg5raNwt++bRpliOzOvLsmw5Iu1sLdf2XgtFGt24dr1sMlkEdgOLPYLqrox1z39+FTlUzSxd9K/" +
            "6ycsGmBg/CYRsi53SjifoNL9lGY/FxJ48ayC4WJNRFhUsMuJiJWLH1Ct5aG2leU1FHslBAsCBfVn0MWhMS86hdWsIhrgqJAfAOOs" +
            "w4ZYd6cbY+pDHRnhFP30bRh44e6qUIrbJzsKoxlR7f/EwDZaFDi4Cp1ruBsemhzmoZvII5g8HzQmHq254RE0qJWAnrxBjBES6nnB" +
            "61QXykIcxWmRiW9pHDj3oGXsQqKhAERZubfrLVmOFYDncQKBgQDOa3xCjpOPAJilz6WdnvsE7go9mfK7xOXa6p/NnK6ss4uU7b/F" +
            "D/qDhAfkv5t3sTvzCAKxlR4f2llfmL86eI1E0kLNioIYcVdZZvU1cCxGlkQOBzFj4jfYlAsEm9NqlVP86Qn3XrAb+xU3M/yJBQmL" +
            "Xf2obk8/5R4GyI+9RGdcAwKBgQDNgiUg0qbQ7Ge+WESoiWGN6AJQmvcURrEK31md7hIiZBFkxVUaDPMfT63cQd3QncxNQ4MWmIYa" +
            "0hA1WqaNmHa38S1usmzC65G6y0r4BNtxr1cNBKGr3ncvFKGo7jfiQDbTKheoWYpjjfLnOLxOvAj3T8PeOIQFkcFOvPHp52DwEQKB" +
            "gQCGSUCmX/C46E9mF0cL2qA83csFtkBGTVqnZ1mcvqCYM5nLwjuurK1xn5QvCUPPwxpYCc8Ozg6x9C+OVGWbWU+2BP92jfQCiMGl" +
            "pKhDIn17Ukhlw+Iqs8FXAUCFuAX64bio4MYeCpiRK2/q4MQaM4+4/eWGDHU8AQberN1t6LFBFwKBgQCX4KDz2vpx2OTq5W0IaU9x" +
            "go+DZqNHlt7svBgNAY7hX7cDFFtKf1uGPmqtJvh5XsVILKbKojopxCk6ZR5F8vF6C4GaF78NZu8yb1+eYGiVYhIhitS4ohbO8p5u" +
            "rG8chHyAHtGRF2uRG6zyeRmhzpGJxKdqqbXYL9n/COB4DwmzIQKBgBbwWYXHAVIhnpK19A5/2uyh1Fqw99QkOnDlauSkv2qdl1Kx" +
            "87NGDTg8rAAYIZ5e9DoLRelNwfUuftjf/VzqwQ4+s9GC5O8RFIEIK6pyate2YnldkSdKaxYk8YIBDMoSQH+LngMVtbz44e9KbbJp" +
            "AMP4/OabOUwxzAdBLobdEreD";

    private static final String WEBCRYPTO_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApbUDBmY4ZXZeeBu/+M7xIFdKC3mR3rdwpgi79kA10XRlWhiyGumg55Hw" +
            "wHte4cSmj66OIHQUu94wLJgzoASTZoxb16t7tFRrm02cHzGi1UbFiVvswSRkLD62YT5dyZI+aQTAy60nGKEpauvQDwkmPKV2cNuw" +
            "z9V5zqtsr9egZznvnXzWtcTL/q7pM2nk/vtEfyW1UloqGrYAKubFF7TkQDugZYPn59A9sIa1BwGruwbaVco3x1Kwt1skKNVW1Ocn" +
            "M9c5PSTIX9JO0XlVm8UH6k2EpZyKHpByS+zG9kyFGdzk2Yj3i0PmE28oDpC2kvd3JtzKrNg2riA1q3g2pz/sMwIDAQAB";

    /** {@code subtle.encrypt({name: "RSA-OAEP"}, publicKey, "admin123")} */
    private static final String WEBCRYPTO_CIPHERTEXT = "KsycTwVadPaM5NoF3QHMNbGvmYNH6yS9z8c1yjNWQIjqgh47dT9lnKje14lCie/6M8rW1/+YRCHkfZ2xFPt2aais3FEqZfJXVTU5" +
            "aIB7GrjG3/l0eiwATwppv/055+LSFloP76/SXuj1n3Ma/HPMA+VYR3wavy7FF+LAz374DYVPiiJFUMX3lVnexCBjgoJFLw9zGE6w" +
            "kHMuT3zDYI81yLXV6QXwqwzxcSx4WIFhDKXtk7CXCnGpqJPliXIwxxjJATWon/P8SxD1GlTxtEQpyXGHTyZrVbwYGalBHMA+ZDwS" +
            "TfBvaxooZwKm0oFmZ+5YLFAjqBDCqJgEqwix5w1AZQ==";

    @Test
    void decryptsWhatTheBrowserEncrypts() throws Exception {
        assertEquals("admin123", RsaEncrypter.decrypt(WEBCRYPTO_CIPHERTEXT, WEBCRYPTO_PRIVATE_KEY));
    }

    @Test
    void roundTripsWithOaep() throws Exception {
        Map<String, String> keys = RsaEncrypter.generateKeyPair();
        String cipher = RsaEncrypter.encrypt("s3cr3t!", java.util.Objects.requireNonNull(keys.get("publicKey")));

        assertEquals("s3cr3t!", RsaEncrypter.decrypt(cipher, java.util.Objects.requireNonNull(keys.get("privateKey"))));
    }

    /** The server offers clients the public key of the private key it is configured with. */
    @Test
    void derivesThePublicKeyFromThePrivateKey() throws Exception {
        assertEquals(WEBCRYPTO_PUBLIC_KEY, RsaEncrypter.publicKeyOf(WEBCRYPTO_PRIVATE_KEY));
    }

    /** PKCS#1 v1.5 ciphertexts (the old scheme) are not accepted any more. */
    @Test
    void rejectsGarbage() {
        assertThrows(Exception.class, () -> RsaEncrypter.decrypt("bm90LWEtY2lwaGVydGV4dA==", WEBCRYPTO_PRIVATE_KEY));
    }
}
