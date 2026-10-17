package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoinAmountTextTest {
    @Test
    public void autoBelowThresholdUsesTokens() {
        long value = CoinUnit.getAutoThreshold() - 1;
        Assertions.assertEquals(CoinUnit.TOKENS, CoinAmountText.resolve(value, CoinUnit.AUTO));
        Assertions.assertEquals(CoinAmountText.tokensLabel(UnitFormat.DOT, value),
                CoinAmountText.display(value, CoinUnit.AUTO, UnitFormat.DOT));
        Assertions.assertTrue(CoinAmountText.display(value, CoinUnit.AUTO, UnitFormat.DOT).endsWith(" tokens"));
        Assertions.assertTrue(CoinAmountText.tooltip(value, CoinUnit.AUTO, UnitFormat.DOT).endsWith(" GFCN"));
    }

    @Test
    public void autoAtThresholdUsesFcn() {
        long value = CoinUnit.getAutoThreshold();
        Assertions.assertEquals(CoinUnit.GFCN, CoinAmountText.resolve(value, CoinUnit.AUTO));
        Assertions.assertEquals(CoinAmountText.coinLabel(UnitFormat.DOT, value),
                CoinAmountText.display(value, CoinUnit.AUTO, UnitFormat.DOT));
        Assertions.assertTrue(CoinAmountText.display(value, CoinUnit.AUTO, UnitFormat.DOT).endsWith(" GFCN"));
        Assertions.assertTrue(CoinAmountText.tooltip(value, CoinUnit.AUTO, UnitFormat.DOT).endsWith(" tokens"));
    }

    @Test
    public void explicitBtcAndTokensIgnoreThreshold() {
        Assertions.assertTrue(CoinAmountText.display(1L, CoinUnit.GFCN, UnitFormat.DOT).endsWith(" GFCN"));
        Assertions.assertTrue(CoinAmountText.display(CoinUnit.getAutoThreshold(), CoinUnit.TOKENS, UnitFormat.DOT)
                .endsWith(" tokens"));
        Assertions.assertEquals(CoinUnit.GFCN, CoinAmountText.resolve(1L, CoinUnit.GFCN));
        Assertions.assertEquals(CoinUnit.TOKENS, CoinAmountText.resolve(CoinUnit.getAutoThreshold(), CoinUnit.TOKENS));
    }

    @Test
    public void nullUnitIsAuto() {
        Assertions.assertEquals(CoinUnit.TOKENS, CoinAmountText.resolve(1L, null));
        Assertions.assertEquals(CoinUnit.GFCN, CoinAmountText.resolve(CoinUnit.getAutoThreshold(), null));
        Assertions.assertTrue(CoinAmountText.display(1L, null, null).endsWith(" tokens"));
    }
}
