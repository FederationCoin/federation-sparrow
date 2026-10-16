package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.InvalidAddressException;
import com.sparrowwallet.drongo.dns.DnsPayment;
import com.sparrowwallet.drongo.dns.DnsPaymentCache;
import com.sparrowwallet.drongo.dns.DnsPaymentResolver;
import com.sparrowwallet.drongo.dns.DnsPaymentValidationException;
import com.sparrowwallet.drongo.silentpayments.SilentPayment;
import com.sparrowwallet.drongo.silentpayments.SilentPaymentAddress;
import com.sparrowwallet.drongo.uri.CoinURIParseException;
import com.sparrowwallet.drongo.wallet.Payment;
import com.sparrowwallet.sparrow.AppServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public sealed interface SendDestination permits SendDestination.ChainAddress, SendDestination.DnsHrn {
    Logger log = LoggerFactory.getLogger(SendDestination.class);
    String SILENT_PAYMENTS_UNAVAILABLE = "Silent payments are not available";

    Payment toPayment(String label, long value, boolean sendMax)
            throws DnsPaymentValidationException, IOException, ExecutionException, InterruptedException, CoinURIParseException;

    boolean needsDnsResolution();

    Address sendableAddress() throws InvalidAddressException;

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R chainAddress(ChainAddress destination) throws InvalidAddressException;

        R dnsHrn(DnsHrn destination) throws InvalidAddressException;
    }

    static SendDestination parse(String value) {
        if(value == null || value.isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if(trimmed.regionMatches(true, 0, com.sparrowwallet.drongo.uri.CoinURI.COIN_SCHEME + ":", 0, com.sparrowwallet.drongo.uri.CoinURI.COIN_SCHEME.length() + 1)) {
            try {
                com.sparrowwallet.drongo.uri.CoinURI uri = new com.sparrowwallet.drongo.uri.CoinURI(trimmed);
                if(uri.getSilentPaymentAddress() != null) {
                    throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
                }
                Address address = uri.getAddress();
                address.requireSendable();
                return new ChainAddress(address);
            } catch(CoinURIParseException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            } catch(InvalidAddressException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        Optional<String> optDnsPaymentHrn = DnsPayment.getHrn(trimmed);
        if(optDnsPaymentHrn.isPresent()) {
            return new DnsHrn(optDnsPaymentHrn.get());
        }
        if(hasSilentPaymentHrp(trimmed)) {
            throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
        }
        try {
            SilentPaymentAddress.from(value);
            throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
        } catch(IllegalArgumentException e) {
            if(SILENT_PAYMENTS_UNAVAILABLE.equals(e.getMessage())) {
                throw e;
            }
            log.debug("Value is not a silent payment address", e);
        } catch(Exception e) {
            log.debug("Value is not a silent payment address", e);
        }
        try {
            Address address = Address.fromString(value);
            address.requireSendable();
            return new ChainAddress(address);
        } catch(InvalidAddressException e) {
            return null;
        }
    }

    static SendDestination fromPayment(Payment payment) {
        DnsPayment dnsPayment = DnsPaymentCache.getDnsPayment(payment);
        if(dnsPayment != null) {
            return new DnsHrn(dnsPayment.hrn());
        }
        if(payment instanceof SilentPayment) {
            throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
        }
        return payment.getAddress() == null ? null : new ChainAddress(payment.getAddress());
    }

    record ChainAddress(Address address) implements SendDestination {
        @Override
        public Payment toPayment(String label, long value, boolean sendMax) {
            try {
                address.requireSendable();
            } catch(InvalidAddressException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return new Payment(address, label, value, sendMax);
        }

        @Override
        public boolean needsDnsResolution() {
            return false;
        }

        @Override
        public Address sendableAddress() throws InvalidAddressException {
            address.requireSendable();
            return address;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            try {
                return visitor.chainAddress(this);
            } catch(InvalidAddressException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return address.toString();
        }
    }

    record DnsHrn(String hrn) implements SendDestination {
        @Override
        public Payment toPayment(String label, long value, boolean sendMax)
                throws DnsPaymentValidationException, IOException, ExecutionException, InterruptedException, CoinURIParseException {
            DnsPayment dnsPayment = DnsPaymentCache.getDnsPayment(hrn);
            if(dnsPayment == null) {
                Optional<DnsPayment> optDnsPayment = new DnsPaymentResolver(hrn).resolve(AppServices.getProxy());
                if(optDnsPayment.isEmpty()) {
                    throw new IllegalArgumentException("Could not resolve " + hrn);
                }
                dnsPayment = optDnsPayment.get();
                cacheIfSendable(dnsPayment);
            }
            return paymentFromDns(dnsPayment, label, value, sendMax);
        }

        @Override
        public boolean needsDnsResolution() {
            return DnsPaymentCache.getDnsPayment(hrn) == null;
        }

        @Override
        public Address sendableAddress() throws InvalidAddressException {
            DnsPayment dnsPayment = DnsPaymentCache.getDnsPayment(hrn);
            if(dnsPayment == null || !dnsPayment.hasAddress()) {
                throw new InvalidAddressException("Could not resolve " + hrn);
            }
            if(dnsPayment.hasSilentPaymentAddress()) {
                throw new InvalidAddressException(SILENT_PAYMENTS_UNAVAILABLE);
            }
            Address resolved = dnsPayment.coinURI().getAddress();
            resolved.requireSendable();
            return resolved;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            try {
                return visitor.dnsHrn(this);
            } catch(InvalidAddressException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return hrn;
        }
    }

    private static boolean hasSilentPaymentHrp(String trimmed) {
        for(Network network : Network.values()) {
            String hrp = network.getSilentPaymentsAddressHrp();
            if(trimmed.length() > hrp.length()
                    && trimmed.regionMatches(true, 0, hrp, 0, hrp.length())
                    && trimmed.charAt(hrp.length()) == '1') {
                return true;
            }
        }
        return false;
    }

    private static void cacheIfSendable(DnsPayment dnsPayment) {
        if(dnsPayment.hasSilentPaymentAddress()) {
            throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
        }
        if(dnsPayment.hasAddress()) {
            Address resolved = dnsPayment.coinURI().getAddress();
            try {
                resolved.requireSendable();
            } catch(InvalidAddressException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            DnsPaymentCache.putDnsPayment(resolved, dnsPayment);
        }
    }

    private static Payment paymentFromDns(DnsPayment dnsPayment, String label, long value, boolean sendMax) {
        if(dnsPayment.hasSilentPaymentAddress()) {
            throw new IllegalArgumentException(SILENT_PAYMENTS_UNAVAILABLE);
        }
        if(!dnsPayment.hasAddress()) {
            throw new IllegalArgumentException("Could not resolve " + dnsPayment);
        }
        Address resolved = dnsPayment.coinURI().getAddress();
        try {
            resolved.requireSendable();
        } catch(InvalidAddressException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return new Payment(resolved, label, value, sendMax);
    }
}
