package net.fuxle.awooapi.component.cryptography;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class JWTTokenUtil {

    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ECPrivateKey jwtPrivateKey = null;
    public ECPublicKey jwtPublicKey = null;
    private Algorithm jwtAlgorithm;
    private String jwtIssuer;


    public void initialize(Path jwtPublicKeyFile, Path jwtPrivateKeyFile, String jwtIssuer) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchProviderException {
        this.jwtIssuer = jwtIssuer;

        log.info("Register BouncyCastle Cryptographic Provider");
        Security.addProvider(new BouncyCastleProvider());

        if (!Files.exists(jwtPublicKeyFile) && !Files.exists(jwtPrivateKeyFile)) {
            log.info("ECDSA Key Files not existing, generating new one...");

            // Wählen Sie eine ECDSA-Kurvenparameter-Spezifikation (z. B. P-256)
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("P-256");

            // Erzeugen Sie den Schlüsselgenerator für ECDSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
            keyPairGenerator.initialize(ecSpec, new SecureRandom());

            // Generieren Sie das Schlüsselpaar
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            //Save keypair to disk
            jwtPublicKey = (ECPublicKey) keyPair.getPublic();
            jwtPrivateKey = (ECPrivateKey) keyPair.getPrivate();


            log.info("\uD83D\uDD12 Key generated, dumping to file");

            KeyStorage.savePublicKey(jwtPublicKey, jwtPublicKeyFile);
            log.info("\uD83D\uDCBE JWT Public Key saved to " + jwtPublicKeyFile);

            KeyStorage.savePrivateKey(jwtPrivateKey, jwtPrivateKeyFile);
            log.info("\uD83D\uDCBE JWT Private Key saved to " + jwtPrivateKeyFile);
        } else {

            log.info("\uD83D\uDDC3\uFE0F Loading JWT Public Key from " + jwtPublicKeyFile);
            jwtPublicKey = (ECPublicKey) KeyStorage.readPublicKey(jwtPublicKeyFile);
            log.info("JWT Public Key loaded");

            log.info("\uD83D\uDDC3\uFE0F Loading JWT Private Key from " + jwtPrivateKeyFile);
            jwtPrivateKey = (ECPrivateKey) KeyStorage.readPrivateKey(jwtPrivateKeyFile);
            log.info("JWT Private Key loaded");

        }
        log.info("Set JWT Algorithm");
        jwtAlgorithm = Algorithm.ECDSA256(jwtPublicKey, jwtPrivateKey);

        log.info("JWT initialization complete");
    }

    public Token generateNewJWTForUser(String sessionId) {
        log.info("Generating JWT for session " + sessionId);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); //Current Date
        c.add(Calendar.DATE, 90); //Add Session Lifetime Days

        Instant notBefore = Instant.now();
        Instant notAfter = c.toInstant();

        String jwtToken = JWT.create()
                .withIssuer(jwtIssuer)
                //Token should expire in X days
                .withExpiresAt(notAfter)
                .withNotBefore(notBefore) //Now
                .withClaim("session", sessionId)
                // .withClaim("ip",ipAddress)
                .sign(jwtAlgorithm);

        return new Token(jwtToken, notBefore, notAfter);

    }

    public DecodedJWT decodeJWT(String jwtToken) throws JWTVerificationException {
        log.info("Decoding JWT " + jwtToken);

        JWTVerifier verifier = JWT.require(jwtAlgorithm)
                // specify an specific claim validations
                .withIssuer(jwtIssuer)
                // reusable verifier instance
                .build();

        DecodedJWT decodedJWT = verifier.verify(jwtToken);
        log.info("Decoding JWT successful");

        return decodedJWT;

    }

    public static class Token {
        private String jwtToken;
        private Instant notBefore;
        private Instant notAfter;

        public Token() {
        }

        public Token(String jwtToken, Instant notBefore, Instant notAfter) {
            this.jwtToken = jwtToken;
            this.notBefore = notBefore;
            this.notAfter = notAfter;
        }

        public String getJwtToken() {
            return jwtToken;
        }

        public void setJwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        public Instant getNotBefore() {
            return notBefore;
        }

        public void setNotBefore(Instant notBefore) {
            this.notBefore = notBefore;
        }

        public Instant getNotAfter() {
            return notAfter;
        }

        public void setNotAfter(Instant notAfter) {
            this.notAfter = notAfter;
        }

    }
}
