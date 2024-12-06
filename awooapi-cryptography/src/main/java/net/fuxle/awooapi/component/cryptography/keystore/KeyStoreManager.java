package net.fuxle.awooapi.component.cryptography.keystore;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

public class KeyStoreManager {
    private static final Logger log = LoggerFactory.getLogger(KeyStoreManager.class);
    private KeyStore keyStore;


    public KeyStoreManager(IKeyStoreConfig keyStoreConfig) throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        if (keyStoreConfig instanceof PKCS11KeyStoreConfig pkcs11Config) {
            String libraryLocation = pkcs11Config.getLibraryPath().toAbsolutePath().toString();

            log.info("Using PKCS#11 KeyStore with native library at {} with slot {}", libraryLocation, pkcs11Config.getSlot());
            keyStore = PKCS11KeyStoreLoader.loadPKCS11Keystore(pkcs11Config.getPassword(), pkcs11Config.getSlot(), libraryLocation);

        }
        if (keyStoreConfig instanceof PKCS12KeyStoreConfig pkcs12Config) {
            log.info("Using PKCS#12 KeyStore at {}", pkcs12Config.getPath().toAbsolutePath().toString());
            keyStore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
            if (Files.exists(pkcs12Config.getPath())) {
                // If the file exists, load the existing KeyStore
                log.info("KeyStore does exist, loading existing into memory");
                try (InputStream is = Files.newInputStream(pkcs12Config.getPath())) {
                    keyStore.load(is, pkcs12Config.getPassword().toCharArray());
                }
            } else {
                // Otherwise, initialize a new KeyStore
                log.info("KeyStore does not exist, creating new KeyStore");
                keyStore.load(null, pkcs12Config.getPassword().toCharArray());

            }
        }
        if(keyStore == null){
            throw new UnsupportedOperationException("KeyStore configuration is unsupported. Only PKCS#11 and PKCS#12 supported!");
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }
}
