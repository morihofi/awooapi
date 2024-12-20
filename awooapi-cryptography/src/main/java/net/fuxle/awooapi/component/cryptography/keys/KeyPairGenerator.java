/*
 * Copyright (c) 2024 Moritz Hofmann <info@morihofi.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.fuxle.awooapi.component.cryptography.keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;

public class KeyPairGenerator {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Generates an RSA KeyPair with the specified key size.
     *
     * @param rsaKeySize The size of the RSA key to generate.
     * @param givenProviderName The name of the provider to use.
     * @return A KeyPair containing the generated RSA public and private keys.
     * @throws NoSuchAlgorithmException If RSA key pair generation is not supported by the security provider.
     * @throws NoSuchProviderException  If the specified security provider is not found.
     */
    public static KeyPair generateRSAKeyPair(int rsaKeySize, String givenProviderName)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        LOG.info("Starting RSA key pair generation with key size {} using provider '{}'", rsaKeySize, givenProviderName);
        try {
            java.security.KeyPairGenerator rsa = java.security.KeyPairGenerator.getInstance("RSA", givenProviderName);
            rsa.initialize(rsaKeySize);
            KeyPair keyPair = rsa.generateKeyPair();
            LOG.info("RSA key pair generated successfully with key size {}", rsaKeySize);
            return keyPair;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            LOG.error("Error during RSA key pair generation: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Generates an ECDSA KeyPair with the specified elliptic curve.
     *
     * @param curveName The name of the elliptic curve to use for key pair generation.
     * @param givenProviderName The name of the provider to use.
     * @return A KeyPair containing the generated ECDSA public and private keys.
     * @throws NoSuchAlgorithmException           If ECDSA key pair generation is not supported by the security provider.
     * @throws NoSuchProviderException            If the specified security provider is not found.
     * @throws InvalidAlgorithmParameterException If the provided curve name is invalid or not supported.
     */
    public static KeyPair generateEcdsaKeyPair(String curveName, String givenProviderName)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        LOG.info("Starting ECDSA key pair generation with curve '{}' using provider '{}'", curveName, givenProviderName);
        try {
            java.security.KeyPairGenerator keyPairGenerator =
                    java.security.KeyPairGenerator.getInstance("ECDSA", givenProviderName);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
            keyPairGenerator.initialize(ecSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            LOG.info("ECDSA key pair generated successfully with curve '{}'", curveName);
            return keyPair;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            LOG.error("Error during ECDSA key pair generation: {}", e.getMessage());
            throw e;
        }
    }
}
