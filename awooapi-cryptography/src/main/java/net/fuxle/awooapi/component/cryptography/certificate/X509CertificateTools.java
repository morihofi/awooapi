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

package net.fuxle.awooapi.component.cryptography.certificate;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for doing basic X509 certificate stuff
 */
public class X509CertificateTools {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Extracts the X.500 Distinguished Name (DN) from an X.509 certificate.
     *
     * @param cert The X.509 certificate from which to extract the DN.
     * @return An X500Name object representing the subject DN of the certificate.
     * @throws CertificateEncodingException If there is an issue encoding the certificate or extracting the DN.
     */
    public static X500Name getX500NameFromX509Certificate(X509Certificate cert) throws CertificateEncodingException {
        return new JcaX509CertificateHolder(cert).getSubject();
    }

    /**
     * Converts a byte array representing an X.509 certificate into an X.509 certificate object.
     *
     * @param certificateBytes The byte array containing the X.509 certificate data.
     * @return An X509Certificate object representing the parsed X.509 certificate.
     * @throws CertificateException If there is an issue parsing the X.509 certificate from the byte array.
     */
    public static X509Certificate convertToX509Cert(byte[] certificateBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
    }

    /**
     * Reads a certificate file in PEM format, converts it to X.509 format, and returns the certificate bytes.
     *
     * @param certificatePath The path to the certificate file in PEM format.
     * @param keyPair         The key pair containing the public key that should match the certificate's public key.
     * @return The bytes of the X.509 certificate.
     * @throws IOException              If there is an issue reading the certificate file.
     * @throws CertificateException     If there is an issue with the certificate format or content.
     * @throws IllegalArgumentException If the certificate's public key does not match the specified public key.
     */
    public static byte[] getCertificateBytes(Path certificatePath, KeyPair keyPair) throws IOException, CertificateException {
        try (PEMParser pemParser = new PEMParser(Files.newBufferedReader(certificatePath))) {
            Object pemObject = pemParser.readObject();
            if (pemObject instanceof X509CertificateHolder certificateHolder) {
                // Convert the read object to an X509Certificate
                X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);

                // Check if the certificate's public key matches the specified public key
                if (!certificate.getPublicKey().equals(keyPair.getPublic())) {
                    throw new IllegalArgumentException("The public certificate does not match the specified public key.");
                }

                // Return the certificate bytes in X.509 format
                return certificate.getEncoded();
            } else {
                throw new IllegalArgumentException("The specified file does not contain a valid certificate.");
            }
        }
    }

    /**
     * Checks the validity of a given X.509 certificate as of the current date and time. This method uses the {@code checkValidity} method
     * of {@link X509Certificate} to determine whether the certificate is currently valid. The validity check is based on the certificate's
     * notBefore and notAfter dates.
     *
     * @param certificate the X.509 certificate to be checked for validity.
     * @return {@code true} if the certificate is currently valid; {@code false} if it is expired or not yet valid as of the current date.
     */
    public static boolean isCertificateDateValid(X509Certificate certificate) {
        try {
            certificate.checkValidity(new Date());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Loads a chain of X.509 certificates from a PEM file and returns them as an array of X.509 certificate objects.
     *
     * @param pemFilePath The path to the PEM file containing the X.509 certificate chain.
     * @return An array of X509Certificate objects representing the parsed X.509 certificate chain.
     * @throws IOException             If there is an issue reading the PEM file.
     * @throws CertificateException    If there is an issue parsing the X.509 certificates from the PEM file.
     * @throws NoSuchProviderException If there is no security provider available for X.509 certificates.
     */
    public static X509Certificate[] loadCertificateChain(Path pemFilePath) throws IOException, CertificateException,
            NoSuchProviderException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);

        try (PEMParser pemParser = new PEMParser(Files.newBufferedReader(pemFilePath))) {
            List<X509Certificate> certificates = new ArrayList<>();
            Object object;

            while ((object = pemParser.readObject()) != null) {
                if (object instanceof X509CertificateHolder certificateHolder) {
                    X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(
                            new ByteArrayInputStream(certificateHolder.getEncoded())
                    );
                    certificates.add(certificate);
                }
            }

            return certificates.toArray(new X509Certificate[0]);
        }
    }


    /**
     * Generates a secure random serial number.
     *
     * @return A BigInteger representing a 160-bit secure random serial number.
     */
    public static BigInteger generateSerialNumber() {
        return new BigInteger(192, new SecureRandom()); // Secure random serial number
    }


    /**
     * Converts an array of byte arrays containing certificates into a PEM-encoded string representing a certificate chain.
     *
     * @param certificateBytesArray An array of byte arrays where each element represents a certificate in the chain.
     * @return The PEM-encoded representation of the certificate chain.
     * @throws CertificateException If there is an issue converting the certificate chain.
     */
    public static String certificatesChainToPEM(byte[][] certificateBytesArray) throws CertificateException {
        try {
            // Create a StringWriter to store the entire certificate chain
            StringWriter stringWriter = new StringWriter();

            // Iterate through each certificate in the chain
            for (byte[] certificateBytes : certificateBytesArray) {
                // Create a PemObject for each certificate
                stringWriter.write(certificateToPEM(certificateBytes));
                // Add a newline between certificates for better readability
                stringWriter.write("\n");
            }

            // Return the entire PEM-encoded representation of the certificate chain
            return stringWriter.toString();
        } catch (Exception e) {
            throw new CertificateException("Error converting the certificate chain to PEM format", e);
        }
    }

    /**
     * Converts a byte array containing a certificate into a PEM-encoded string.
     *
     * @param certificateBytes The byte array representing the certificate.
     * @return The PEM-encoded representation of the certificate.
     * @throws IOException If there is an issue converting the certificate.
     */
    public static String certificateToPEM(byte[] certificateBytes) throws IOException {
        // Create a PemObject with the certificate bytes
        PemObject pemObject = new PemObject("CERTIFICATE", certificateBytes);

        // Create a StringWriter and a PemWriter
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            // Write the PemObject to the PemWriter
            pemWriter.writeObject(pemObject);
        }

        // Return the PEM-encoded representation of the certificate
        return stringWriter.toString();
    }

    /**
     * Private constructor to prevent object creation
     */
    private X509CertificateTools() {}
}
