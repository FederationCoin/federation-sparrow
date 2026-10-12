package com.sparrowwallet.sparrow.event;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.io.Config;

public class UnitFormatChangedEvent {
    private final UnitFormat unitFormat;

    public UnitFormatChangedEvent(UnitFormat unitFormat) {
        this.unitFormat = unitFormat;
    }

    public UnitFormat getUnitFormat() {
        return unitFormat;
    }

    public CoinUnit getCoinUnit() {
        return Config.get().getCoinUnit();
    }
}
