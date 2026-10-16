package com.sparrowwallet.sparrow.event;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.io.Config;

public class CoinUnitChangedEvent extends UnitFormatChangedEvent {
    private final CoinUnit coinUnit;

    public CoinUnitChangedEvent(CoinUnit coinUnit) {
        super(Config.get().getUnitFormat() == null ? UnitFormat.DOT : Config.get().getUnitFormat());
        this.coinUnit = coinUnit;
    }

    public CoinUnitChangedEvent(UnitFormat unitFormat, CoinUnit coinUnit) {
        super(unitFormat);
        this.coinUnit = coinUnit;
    }

    public CoinUnit getCoinUnit() {
        return coinUnit;
    }
}
