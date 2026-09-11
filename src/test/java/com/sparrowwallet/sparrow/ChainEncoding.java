package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.crypto.ECKey;
import com.sparrowwallet.drongo.protocol.Base58;
import com.sparrowwallet.drongo.protocol.Bech32;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

/**
 * Re-encodes published Bitcoin address / WIF vectors for this chain's version bytes and HRPs.
 *
 * The payload (hash160, witness program, or scalar) is unchanged. Tests keep the published string as
 * the identity of the vector and compare against this encoding rather than Bitcoin's.
 */
public final class ChainEncoding {
    private ChainEncoding() {
    }

    public static String address(String published) {
        String lower = published.toLowerCase(Locale.ROOT);
        if(lower.startsWith("bc1") || lower.startsWith("tb1") || lower.startsWith("bcrt1")) {
            Bech32.Bech32Data data = Bech32.decode(published);
            String encoded = Bech32.encode(Network.get().getBech32AddressHRP(), data.encoding, data.data);
            return published.equals(published.toUpperCase(Locale.ROOT)) ? encoded.toUpperCase(Locale.ROOT) : encoded;
        }
        if(lower.startsWith("sp1") || lower.startsWith("tsp1")) {
            Bech32.Bech32Data data = Bech32.decode(published, 1023);
            return Bech32.encode(Network.get().getSilentPaymentsAddressHrp(), data.encoding, data.data);
        }
        if(lower.startsWith("spscan1") || lower.startsWith("tspscan1") || lower.startsWith("bcrtscan1")) {
            Bech32.Bech32Data data = Bech32.decode(published, 1023);
            return Bech32.encode(Network.get().getSilentPaymentsScanKeyHrp(), data.encoding, data.data);
        }

        byte[] decoded = Base58.decodeChecked(published);
        int version = decoded[0] & 0xff;
        byte[] hash = Arrays.copyOfRange(decoded, 1, decoded.length);
        int ourVersion = switch(version) {
            case 0, 111 -> Network.get().getP2PKHAddressHeader();
            case 5, 196 -> Network.get().getP2SHAddressHeader();
            default -> throw new IllegalArgumentException("Unsupported published address version " + version + " for " + published);
        };
        return Base58.encodeChecked(ourVersion, hash);
    }

    /**
     * The 32-byte scalar of a published Bitcoin WIF. Version 128/239 is Bitcoin; this chain uses 164/223.
     */
    public static ECKey keyFromPublishedWif(String bitcoinWif) {
        byte[] decoded = Base58.decodeChecked(bitcoinWif);
        boolean compressed = decoded.length == 34 && decoded[33] == 1;
        byte[] keyBytes = Arrays.copyOfRange(decoded, 1, 33);
        return ECKey.fromPrivate(new BigInteger(1, keyBytes), compressed);
    }
}
