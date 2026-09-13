package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.BitcoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoinAmountTextTest {
    @Test
    public void autoBelowThresholdUsesTokens() {
        long value = BitcoinUnit.getAutoThreshold() - 1;
        Assertions.assertEquals(BitcoinUnit.SATOSHIS, CoinAmountText.resolve(value, BitcoinUnit.AUTO));
        Assertions.assertEquals(CoinAmountText.tokensLabel(UnitFormat.DOT, value),
                CoinAmountText.display(value, BitcoinUnit.AUTO, UnitFormat.DOT));
        Assertions.assertTrue(CoinAmountText.display(value, BitcoinUnit.AUTO, UnitFormat.DOT).endsWith(" tokens"));
        Assertions.assertTrue(CoinAmountText.tooltip(value, BitcoinUnit.AUTO, UnitFormat.DOT).endsWith(" FCN"));
    }

    @Test
    public void autoAtThresholdUsesFcn() {
        long value = BitcoinUnit.getAutoThreshold();
        Assertions.assertEquals(BitcoinUnit.BTC, CoinAmountText.resolve(value, BitcoinUnit.AUTO));
        Assertions.assertEquals(CoinAmountText.btcLabel(UnitFormat.DOT, value),
                CoinAmountText.display(value, BitcoinUnit.AUTO, UnitFormat.DOT));
        Assertions.assertTrue(CoinAmountText.display(value, BitcoinUnit.AUTO, UnitFormat.DOT).endsWith(" FCN"));
        Assertions.assertTrue(CoinAmountText.tooltip(value, BitcoinUnit.AUTO, UnitFormat.DOT).endsWith(" tokens"));
    }

    @Test
    public void explicitBtcAndTokensIgnoreThreshold() {
        Assertions.assertTrue(CoinAmountText.display(1L, BitcoinUnit.BTC, UnitFormat.DOT).endsWith(" FCN"));
        Assertions.assertTrue(CoinAmountText.display(BitcoinUnit.getAutoThreshold(), BitcoinUnit.SATOSHIS, UnitFormat.DOT)
                .endsWith(" tokens"));
        Assertions.assertEquals(BitcoinUnit.BTC, CoinAmountText.resolve(1L, BitcoinUnit.BTC));
        Assertions.assertEquals(BitcoinUnit.SATOSHIS, CoinAmountText.resolve(BitcoinUnit.getAutoThreshold(), BitcoinUnit.SATOSHIS));
    }

    @Test
    public void nullUnitIsAuto() {
        Assertions.assertEquals(BitcoinUnit.SATOSHIS, CoinAmountText.resolve(1L, null));
        Assertions.assertEquals(BitcoinUnit.BTC, CoinAmountText.resolve(BitcoinUnit.getAutoThreshold(), null));
        Assertions.assertTrue(CoinAmountText.display(1L, null, null).endsWith(" tokens"));
    }
}
