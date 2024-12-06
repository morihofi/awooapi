package net.fuxle.awooapi.component.cryptography.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class KeyPairGeneratorTest {

    @BeforeAll
    static void setup() {
        // Register Bouncy Castle as a security provider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testGenerateRSAKeyPairWithBouncyCastle() {
        int rsaKeySize = 2048; // Common RSA key size
        String providerName = BouncyCastleProvider.PROVIDER_NAME;

        try {
            KeyPair keyPair = KeyPairGenerator.generateRSAKeyPair(rsaKeySize, providerName);
            assertNotNull(keyPair, "Generated RSA KeyPair should not be null");
            assertNotNull(keyPair.getPublic(), "Generated RSA public key should not be null");
            assertNotNull(keyPair.getPrivate(), "Generated RSA private key should not be null");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            fail("Exception during RSA key pair generation with Bouncy Castle: " + e.getMessage());
        }
    }

    @Test
    void testGenerateEcdsaKeyPairWithBouncyCastle() {
        String curveName = "secp256r1"; // Common elliptic curve
        String providerName = BouncyCastleProvider.PROVIDER_NAME;

        try {
            KeyPair keyPair = KeyPairGenerator.generateEcdsaKeyPair(curveName, providerName);
            assertNotNull(keyPair, "Generated ECDSA KeyPair should not be null");
            assertNotNull(keyPair.getPublic(), "Generated ECDSA public key should not be null");
            assertNotNull(keyPair.getPrivate(), "Generated ECDSA private key should not be null");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            fail("Exception during ECDSA key pair generation with Bouncy Castle: " + e.getMessage());
        }
    }

    @Test
    void testGenerateRSAKeyPairWithInvalidProvider() {
        int rsaKeySize = 2048;
        String invalidProviderName = "InvalidProvider";

        assertThrows(NoSuchProviderException.class, () -> {
            KeyPairGenerator.generateRSAKeyPair(rsaKeySize, invalidProviderName);
        }, "Expected NoSuchProviderException for an invalid provider");
    }

    @Test
    void testGenerateEcdsaKeyPairWithInvalidCurve() {
        String invalidCurveName = "invalidCurve";
        String providerName = BouncyCastleProvider.PROVIDER_NAME;

        assertThrows(InvalidAlgorithmParameterException.class, () -> {
            KeyPairGenerator.generateEcdsaKeyPair(invalidCurveName, providerName);
        }, "Expected InvalidAlgorithmParameterException for an invalid curve");
    }
}
