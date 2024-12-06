module awooapi.cryptography {
    requires org.slf4j;
    requires com.auth0.jwt;
    requires org.bouncycastle.pkix;
    requires com.google.gson;
    requires org.bouncycastle.tls;
    requires org.bouncycastle.provider;

    exports net.fuxle.awooapi.component.cryptography.keystore;
    exports net.fuxle.awooapi.component.cryptography.certificate;
    exports net.fuxle.awooapi.component.cryptography.utils;
}
