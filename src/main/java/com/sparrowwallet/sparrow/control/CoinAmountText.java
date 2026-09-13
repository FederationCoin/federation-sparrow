package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.BitcoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;

/**
 * Amount labels shown next to a value. FCN is the coin; tokens are the base unit.
 */
public final class CoinAmountText {
    private CoinAmountText() {
    }

    public static BitcoinUnit resolve(long value, BitcoinUnit bitcoinUnit) {
        BitcoinUnit unit = bitcoinUnit;
        if(unit == null || unit.equals(BitcoinUnit.AUTO)) {
            unit = (value >= BitcoinUnit.getAutoThreshold() ? BitcoinUnit.BTC : BitcoinUnit.SATOSHIS);
        }
        return unit;
    }

    public static String btcLabel(UnitFormat format, long value) {
        UnitFormat f = format == null ? UnitFormat.DOT : format;
        return f.formatBtcValue(value) + " FCN";
    }

    public static String tokensLabel(UnitFormat format, long value) {
        UnitFormat f = format == null ? UnitFormat.DOT : format;
        return f.formatSatsValue(value) + " tokens";
    }

    public static String display(long value, BitcoinUnit bitcoinUnit, UnitFormat format) {
        if(resolve(value, bitcoinUnit).equals(BitcoinUnit.BTC)) {
            return btcLabel(format, value);
        }
        return tokensLabel(format, value);
    }

    public static String tooltip(long value, BitcoinUnit bitcoinUnit, UnitFormat format) {
        if(resolve(value, bitcoinUnit).equals(BitcoinUnit.BTC)) {
            return tokensLabel(format, value);
        }
        return btcLabel(format, value);
    }
}
