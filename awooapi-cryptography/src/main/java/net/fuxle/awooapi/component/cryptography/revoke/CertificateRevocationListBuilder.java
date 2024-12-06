package net.fuxle.awooapi.component.cryptography.revoke;

import net.fuxle.awooapi.component.cryptography.keys.KeyUtil;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.security.PrivateKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CertificateRevocationListBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final X509Certificate caCertificate;
    private final PrivateKey caPrivateKey;
    private final List<RevocationEntry> revocationEntries = new ArrayList<>();
    private ZonedDateTime issueDate = ZonedDateTime.now(); // Default to now
    private long updateInterval = 60; // Default to 60
    private TimeUnit updateIntervalUnit = TimeUnit.MINUTES; // Default to minutes

    public CertificateRevocationListBuilder(X509Certificate caCertificate, PrivateKey caPrivateKey) {
        this.caCertificate = caCertificate;
        this.caPrivateKey = caPrivateKey;
    }

    /**
     * Adds a revocation entry to the CRL.
     *
     * @param entry A {@link RevocationEntry} representing a revoked certificate.
     * @return The builder instance for chaining.
     */
    public CertificateRevocationListBuilder addRevocationEntry(RevocationEntry entry) {
        LOG.debug("Adding revocation entry: SerialNumber={}, RevocationDate={}, Reason={}",
                entry.getSerialNumber(), entry.getRevocationDate(), entry.getRevocationReason());
        revocationEntries.add(entry);
        return this;
    }

    /**
     * Adds multiple revocation entries to the CRL.
     *
     * @param entries A list of {@link RevocationEntry} objects representing revoked certificates.
     * @return The builder instance for chaining.
     */
    public CertificateRevocationListBuilder addRevocationEntries(List<RevocationEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            LOG.warn("No revocation entries provided to add.");
            return this;
        }
        LOG.debug("Adding {} revocation entries", entries.size());
        for (RevocationEntry entry : entries) {
            LOG.debug("Adding revocation entry: SerialNumber={}, RevocationDate={}, Reason={}",
                    entry.getSerialNumber(), entry.getRevocationDate(), entry.getRevocationReason());
        }
        revocationEntries.addAll(entries);
        return this;
    }




    /**
     * Sets the issue date for the CRL.
     *
     * @param issueDate The issue date as {@link ZonedDateTime}.
     * @return The builder instance for chaining.
     */
    public CertificateRevocationListBuilder setIssueDate(ZonedDateTime issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    /**
     * Sets the update interval for the CRL and its time unit.
     *
     * @param interval The update interval.
     * @param timeUnit The time unit for the update interval.
     * @return The builder instance for chaining.
     */
    public CertificateRevocationListBuilder setUpdateInterval(long interval, TimeUnit timeUnit) {
        this.updateInterval = interval;
        this.updateIntervalUnit = timeUnit;
        return this;
    }

    /**
     * Builds and generates the X509CRL object.
     *
     * @return The generated {@link X509CRL}.
     * @throws CertificateEncodingException if an encoding error occurs with the CA certificate.
     * @throws CRLException                 if an error occurs during the CRL generation.
     * @throws OperatorCreationException    if there's an error in creating the content signer.
     */
    public X509CRL build() throws CertificateEncodingException, CRLException, OperatorCreationException {
        LOG.info("Generating CRL with {} revoked certificates", revocationEntries.size());

        // Initialize CRL builder
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(
                new JcaX509CertificateHolder(caCertificate).getSubject(),
                java.util.Date.from(issueDate.toInstant())
        );

        // Calculate the next update date
        long updateIntervalMillis = updateIntervalUnit.toMillis(updateInterval);
        ZonedDateTime nextUpdate = issueDate.plusNanos(TimeUnit.MILLISECONDS.toNanos(updateIntervalMillis));
        crlBuilder.setNextUpdate(java.util.Date.from(nextUpdate.toInstant()));

        LOG.debug("CRL issue date: {}, next update: {}",
                issueDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                nextUpdate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

        // Add revocation entries
        for (RevocationEntry entry : revocationEntries) {
            crlBuilder.addCRLEntry(entry.getSerialNumber(),
                    java.util.Date.from(entry.getRevocationDate().toInstant()),
                    entry.getRevocationReason());
        }

        // Sign the CRL
        String signatureAlgorithm = KeyUtil.getSignatureAlgorithmBasedOnKeyType(caPrivateKey);
        LOG.debug("Using signature algorithm: {}", signatureAlgorithm);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
        signerBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);

        X509CRLHolder crlHolder = crlBuilder.build(signerBuilder.build(caPrivateKey));

        // Convert the CRL to a Java CRL object
        JcaX509CRLConverter converter = new JcaX509CRLConverter();
        converter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        X509CRL crl = converter.getCRL(crlHolder);

        LOG.info("CRL generation completed successfully");
        return crl;
    }
}
