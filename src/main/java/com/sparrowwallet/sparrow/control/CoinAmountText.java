package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;

/**
 * Amount labels shown next to a value. GFCN is the coin; tokens are the base unit.
 */
public final class CoinAmountText {
    private CoinAmountText() {
    }

    public static CoinUnit resolve(long value, CoinUnit coinUnit) {
        CoinUnit unit = coinUnit;
        if(unit == null || unit.equals(CoinUnit.AUTO)) {
            unit = (value >= CoinUnit.getAutoThreshold() ? CoinUnit.GFCN : CoinUnit.TOKENS);
        }
        return unit;
    }

    public static String coinLabel(UnitFormat format, long value) {
        UnitFormat f = format == null ? UnitFormat.DOT : format;
        return f.formatCoinValue(value) + " GFCN";
    }

    public static String tokensLabel(UnitFormat format, long value) {
        UnitFormat f = format == null ? UnitFormat.DOT : format;
        return f.formatTokensValue(value) + " tokens";
    }

    public static String display(long value, CoinUnit coinUnit, UnitFormat format) {
        if(resolve(value, coinUnit).equals(CoinUnit.GFCN)) {
            return coinLabel(format, value);
        }
        return tokensLabel(format, value);
    }

    public static String tooltip(long value, CoinUnit coinUnit, UnitFormat format) {
        if(resolve(value, coinUnit).equals(CoinUnit.GFCN)) {
            return tokensLabel(format, value);
        }
        return coinLabel(format, value);
    }
}
