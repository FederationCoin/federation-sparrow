package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.io.Config;
import javafx.scene.chart.NumberAxis;
import javafx.util.StringConverter;

import java.text.ParseException;

final class CoinAxisFormatter extends StringConverter<Number> {
    private final UnitFormat unitFormat;
    private final CoinUnit coinUnit;

    public CoinAxisFormatter(NumberAxis axis, UnitFormat format, CoinUnit unit) {
        this.unitFormat = format;
        this.coinUnit = unit;
    }

    @Override
    public String toString(Number object) {
        if(Config.get().isHideAmounts()) {
            return "";
        }

        Double value = coinUnit.getValue(object.longValue());
        return new CoinTextFormatter(unitFormat).getCoinFormat().format(value);
    }

    @Override
    public Number fromString(String string) {
        try {
            Number number = new CoinTextFormatter(unitFormat).getCoinFormat().parse(string);
            return coinUnit.getTokensValue(number.doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
