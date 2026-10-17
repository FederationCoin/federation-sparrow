package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.ExtendedKey;
import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.crypto.ECKey;
import com.sparrowwallet.drongo.protocol.Base58;
import com.sparrowwallet.drongo.protocol.Bech32;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Re-encodes published Bitcoin address / WIF vectors for this chain's version bytes and HRPs.
 *
 * The payload (hash160, witness program, or scalar) is unchanged. Tests keep the published string as
 * the identity of the vector and compare against this encoding rather than Bitcoin's.
 */
public final class ChainEncoding {
    private ChainEncoding() {
    }

    private static final String INPUT_CHARSET = "0123456789()[],'/*abcdefgh@:$%{}IJKLMNOPQRSTUVWXYZ&+-.;<=>?!^_|~ijklmnopqrstuvwxyzABCDEFGH`#\"\\ ";
    private static final String CHECKSUM_CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final Pattern EXT_KEY_TOKEN = Pattern.compile("[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{107,112}");
    private static final Pattern CHECKSUM_PATTERN = Pattern.compile("#([" + CHECKSUM_CHARSET + "]{8})$");

    /**
     * Published Bitcoin xpub/xprv/tpub/tprv, re-encoded with this chain's BIP32 version bytes.
     * SLIP-132 and keys already on this chain are unchanged.
     */
    public static String extendedKey(String published) {
        byte[] decoded = Base58.decodeChecked(published);
        if(decoded.length != 78) {
            throw new IllegalArgumentException("Not an extended key");
        }
        int version = ByteBuffer.wrap(decoded).getInt();
        if(version != 0x0488B21E && version != 0x0488ADE4 && version != 0x043587CF && version != 0x04358394) {
            return published;
        }
        boolean priv = decoded[45] == 0x00;
        int header = (priv ? Network.get().getXprvHeader() : Network.get().getXpubHeader()).getHeader();
        ByteBuffer.wrap(decoded).putInt(0, header);
        return Base58.encodeChecked(decoded);
    }

    public static ExtendedKey fromPublishedDescriptor(String published) {
        return ExtendedKey.fromDescriptor(extendedKey(published));
    }

    /**
     * Re-encode Bitcoin xpub/xprv/tpub/tprv tokens in a descriptor or export file. A valid 8-character checksum is recomputed.
     */
    public static String descriptor(String desc) {
        String checksumSuffix = "";
        String body = desc;
        Matcher checksumTail = CHECKSUM_PATTERN.matcher(desc);
        if(checksumTail.find()) {
            checksumSuffix = desc.substring(checksumTail.start());
            body = desc.substring(0, checksumTail.start());
        }
        Matcher matcher = EXT_KEY_TOKEN.matcher(body);
        StringBuilder out = new StringBuilder();
        while(matcher.find()) {
            String token = matcher.group();
            String replacement = token;
            try {
                replacement = extendedKey(token);
            } catch(Exception e) {
                // Leave SLIP-132 and non-keys unchanged.
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        String recoded = out.toString();
        if(checksumSuffix.length() == 9) {
            String origCs = checksumSuffix.substring(1);
            if(descriptorChecksum(body).equals(origCs)) {
                return recoded + "#" + descriptorChecksum(recoded);
            }
            return recoded + checksumSuffix;
        }
        return recoded + checksumSuffix;
    }

    private static String descriptorChecksum(String descriptor) {
        BigInteger c = BigInteger.valueOf(1);
        int cls = 0;
        int clscount = 0;
        for(int i = 0; i < descriptor.length(); i++) {
            int pos = INPUT_CHARSET.indexOf(descriptor.charAt(i));
            if(pos < 0) {
                continue;
            }
            c = descriptorPolyMod(c, pos & 31);
            cls = cls * 3 + (pos >> 5);
            if(++clscount == 3) {
                c = descriptorPolyMod(c, cls);
                cls = 0;
                clscount = 0;
            }
        }
        if(clscount > 0) {
            c = descriptorPolyMod(c, cls);
        }
        for(int j = 0; j < 8; ++j) {
            c = descriptorPolyMod(c, 0);
        }
        c = c.xor(BigInteger.valueOf(1));
        StringBuilder ret = new StringBuilder();
        for(int j = 0; j < 8; ++j) {
            ret.append(CHECKSUM_CHARSET.charAt(c.shiftRight(5 * (7 - j)).and(BigInteger.valueOf(31)).intValue()));
        }
        return ret.toString();
    }

    private static BigInteger descriptorPolyMod(BigInteger c, int val) {
        byte c0 = c.shiftRight(35).byteValue();
        c = c.and(new BigInteger("7ffffffff", 16)).shiftLeft(5).xor(BigInteger.valueOf(val));
        if((c0 & 1) > 0) {
            c = c.xor(new BigInteger("f5dee51989", 16));
        }
        if((c0 & 2) > 0) {
            c = c.xor(new BigInteger("a9fdca3312", 16));
        }
        if((c0 & 4) > 0) {
            c = c.xor(new BigInteger("1bab10e32d", 16));
        }
        if((c0 & 8) > 0) {
            c = c.xor(new BigInteger("3706b1677a", 16));
        }
        if((c0 & 16) > 0) {
            c = c.xor(new BigInteger("644d626ffd", 16));
        }
        return c;
    }

    public static String address(String published) {
        String lower = published.toLowerCase(Locale.ROOT);
        if(lower.startsWith("bc1") || lower.startsWith("tb1") || lower.startsWith("bcrt1")
                || lower.startsWith("gfcn1") || lower.startsWith("tgfcn1") || lower.startsWith("gfcnrt1")) {
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
