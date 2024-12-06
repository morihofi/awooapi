package net.fuxle.awooapi.component.cryptography.keys;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtil {
    /**
     * Determines the appropriate signature algorithm based on the type of the provided private key.
     *
     * @param privateKey The private key for which the signature algorithm needs to be determined.
     * @return A String representing the signature algorithm.
     * @throws IllegalArgumentException If the private key is of a non-supported type.
     */
    public static String getSignatureAlgorithmBasedOnKeyType(PrivateKey privateKey) {
        String signatureAlgorithm;
        if (privateKey instanceof RSAPrivateKey) {
            signatureAlgorithm = "SHA256withRSA";
        } else if (privateKey instanceof ECPrivateKey) {
            signatureAlgorithm = "SHA256withECDSA";
        } else if (privateKey.getClass().getName().equals("sun.security.pkcs11.P11Key$P11PrivateKey")) {
            //FIXME
            // Assuming RSA key for PKCS#11 - may need to be adjusted based on actual key type and capabilities
            signatureAlgorithm = "SHA256withRSA";
        } else {
            throw new IllegalArgumentException("Unsupported key type: " + privateKey.getClass().getName());
        }
        return signatureAlgorithm;
    }

    /**
     * Reads a public key from a PEM-encoded string and returns it as a PublicKey object.
     *
     * @param pemKey The PEM-encoded public key string. (Not File Path)
     * @return A PublicKey object representing the parsed public key.
     * @throws IOException              If there is an issue reading the PEM-encoded key.
     * @throws NoSuchAlgorithmException If the key algorithm is not supported.
     * @throws NoSuchProviderException  If there is no security provider available for the key algorithm.
     * @throws InvalidKeySpecException  If there is an issue parsing the key specification.
     */
    public static PublicKey readPublicKeyFromPem(String pemKey) throws IOException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeySpecException {
        PEMParser pemParser = new PEMParser(new StringReader(pemKey));
        Object object = pemParser.readObject();
        pemParser.close();

        SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) object;
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyInfo.getEncoded());

        String algorithm = publicKeyInfo.getAlgorithm().getAlgorithm().getId();
        KeyFactory keyFactory;

        if (algorithm.equals("1.2.840.10045.2.1")) { // ECDSA
            keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        } else if (algorithm.equals("1.2.840.113549.1.1.1")) { // RSA
            keyFactory = KeyFactory.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        } else {
            throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
        }

        return keyFactory.generatePublic(publicKeySpec);
    }


    /**
     * Saves a public key to a file.
     *
     * @param publicKey The public key to save.
     * @param filePath The path to the file where the key will be saved.
     * @throws IOException If an I/O error occurs.
     */
    public static void savePublicKeyToPem(PublicKey publicKey, Path filePath) throws IOException {
        PemObject pemObject = new PemObject("PUBLIC KEY", publicKey.getEncoded());
        try (PemWriter pemWriter = new JcaPEMWriter(new FileWriter(filePath.toFile()))) {
            pemWriter.writeObject(pemObject);
        }
    }

    /**
     * Saves a public key to a string.
     *
     * @param publicKey The public key to save.
     * @return The PEM-formatted public key as a string.
     * @throws IOException If an I/O error occurs.
     */
    public static String savePublicKeyToPemString(PublicKey publicKey) throws IOException {
        PemObject pemObject = new PemObject("PUBLIC KEY", publicKey.getEncoded());
        StringWriter sw = new StringWriter();
        try (PemWriter pemWriter = new JcaPEMWriter(sw)) {
            pemWriter.writeObject(pemObject);
        }
        return sw.toString();
    }

    /**
     * Saves a private key to a file.
     *
     * @param privateKey The private key to save.
     * @param filePath The path to the file where the key will be saved.
     * @throws IOException If an I/O error occurs.
     */
    public static void savePrivateKeyToPem(PrivateKey privateKey, Path filePath) throws IOException {
        PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());
        try (PemWriter pemWriter = new JcaPEMWriter(new FileWriter(filePath.toFile()))) {
            pemWriter.writeObject(pemObject);
        }
    }

    /**
     * Reads a public key from a file.
     *
     * @param filePath The path to the file containing the public key.
     * @return The public key.
     * @throws IOException If an I/O error occurs.
     * @throws InvalidKeySpecException If the key specification is invalid.
     */
    public static PublicKey readPublicKeyFromPem(Path filePath) throws IOException, InvalidKeySpecException {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath.toFile()))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            Object pemObject = pemParser.readObject();
            if (pemObject instanceof SubjectPublicKeyInfo) {
                return converter.getPublicKey((SubjectPublicKeyInfo) pemObject);
            } else if (pemObject instanceof PEMKeyPair pemKeyPair) {
                return converter.getPublicKey(pemKeyPair.getPublicKeyInfo());
            } else {
                throw new InvalidKeySpecException("Invalid PEM file format: " + filePath);
            }
        }
    }

    /**
     * Reads a private key from a file.
     *
     * @param filePath The path to the file containing the private key.
     * @return The private key.
     * @throws IOException If an I/O error occurs.
     */
    public static PrivateKey readPrivateKeyFromPem(Path filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath.toFile());
        PEMParser pemParser = new PEMParser(fileReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        Object object = pemParser.readObject();
        if (object instanceof PrivateKeyInfo privateKeyInfo) {
            return converter.getPrivateKey(privateKeyInfo);
        } else {
            throw new IllegalArgumentException("Invalid private key format.");
        }
    }

    /**
     * Saves a KeyPair to PEM-encoded files.
     *
     * @param keyPair            The KeyPair to be saved.
     * @param publicKeyFilePath  The path to save the public key in PEM format.
     * @param privateKeyFilePath The path to save the private key in PEM format.
     * @throws IOException If there is an issue writing the keys to the files.
     */
    public static void saveKeyPairToPem(KeyPair keyPair, Path publicKeyFilePath, Path privateKeyFilePath) throws IOException {
        try (JcaPEMWriter writer = new JcaPEMWriter(
                new OutputStreamWriter(Files.newOutputStream(privateKeyFilePath), StandardCharsets.UTF_8))) {
            writer.writeObject(keyPair.getPrivate());
        }

        try (JcaPEMWriter writer = new JcaPEMWriter(
                new OutputStreamWriter(Files.newOutputStream(publicKeyFilePath), StandardCharsets.UTF_8))) {
            writer.writeObject(keyPair.getPublic());
        }
    }

    /**
     * Reads a private key from a PEM-encoded file.
     *
     * @param file The path to the PEM-encoded file containing the private key.
     * @return The PrivateKey read from the file.
     * @throws IOException              If there is an issue reading the file or parsing the key.
     * @throws IllegalArgumentException If the file does not contain a valid private key.
     */
    private static PrivateKey readPrivateKeyFromPemFile(Path file) throws IOException {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
            Object object = pemParser.readObject();

            if (object instanceof PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);
            } else if (object instanceof PEMKeyPair pemKeyPair) {
                return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else {
                throw new IllegalArgumentException("File does not contain a valid private key");
            }
        }
    }

    /**
     * Reads a public key from a PEM-encoded file.
     *
     * @param file The path to the PEM-encoded file containing the public key.
     * @return The PublicKey read from the file.
     * @throws IOException If there is an issue reading the file or parsing the key.
     */
    private static PublicKey readPublicKeyFromPemFile(Path file) throws IOException {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
            Object object = pemParser.readObject();
            return converter.getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) object);
        }
    }

    /**
     * Loads a KeyPair from separate files containing the private and public keys.
     *
     * @param privateKeyFile The path to the file containing the private key.
     * @param publicKeyFile  The path to the file containing the public key.
     * @return A KeyPair containing the loaded public and private keys.
     * @throws IOException If there is an issue reading the key files.
     */
    public static KeyPair loadKeyPairFromPemFiles(Path privateKeyFile, Path publicKeyFile) throws IOException {
        PrivateKey privateKey = readPrivateKeyFromPemFile(privateKeyFile);
        PublicKey publicKey = readPublicKeyFromPemFile(publicKeyFile);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Converts a public key to its PEM format representation.
     *
     * @param publicKey The public key to be converted to PEM format.
     * @return The PEM format representation of the public key.
     * @throws IOException If there is an issue during the conversion process.
     */
    public static String convertToPem(PublicKey publicKey) throws IOException {
        StringWriter stringWriter = new StringWriter();

        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        }

        return stringWriter.toString();
    }

    /**
     * Converts a PEM-encoded string to a byte array containing the binary content.
     *
     * @param pemString The PEM-encoded string to be converted.
     * @return A byte array containing the binary content from the PEM-encoded string.
     * @throws IOException If there is an issue during the conversion process.
     */
    public static byte[] convertPemToByteArray(String pemString) throws IOException {
        PemReader pemReader = new PemReader(new StringReader(pemString));
        PemObject pemObject = pemReader.readPemObject();
        byte[] content = pemObject.getContent();
        pemReader.close();

        return content;
    }


}
