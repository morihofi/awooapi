package net.fuxle.awooapi.component.cryptography.revoke;

import java.math.BigInteger;
import java.util.Date;

public class RevocationEntry {
    private final BigInteger serialNumber;
    private final Date revocationDate;
    private final int revocationReason;

    public RevocationEntry(BigInteger serialNumber, Date revocationDate, int revocationReason) {
        this.serialNumber = serialNumber;
        this.revocationDate = revocationDate;
        this.revocationReason = revocationReason;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public Date getRevocationDate() {
        return revocationDate;
    }

    public int getRevocationReason() {
        return revocationReason;
    }
}
