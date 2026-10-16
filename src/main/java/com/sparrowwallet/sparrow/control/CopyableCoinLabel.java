package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.io.Config;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;

public class CopyableCoinLabel extends CopyableLabel {
    private final LongProperty valueProperty = new SimpleLongProperty(-1);
    private final Tooltip tooltip;
    private final CoinContextMenu contextMenu;

    private CoinUnit coinUnit;

    public CopyableCoinLabel() {
        this("Unknown");
    }

    public CopyableCoinLabel(String text) {
        super(text);
        valueProperty().addListener((observable, oldValue, newValue) -> setValueAsText((Long)newValue, Config.get().getUnitFormat(), Config.get().getCoinUnit()));

        setOnMouseClicked(event -> {
            if(!event.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }

            if(coinUnit == null) {
                coinUnit = Config.get().getCoinUnit();
            }

            if(coinUnit == CoinUnit.TOKENS) {
                coinUnit = CoinUnit.GFCN;
            } else {
                coinUnit = CoinUnit.TOKENS;
            }

            refresh(Config.get().getUnitFormat(), coinUnit);
        });

        tooltip = new Tooltip();
        contextMenu = new CoinContextMenu();
    }

    public final LongProperty valueProperty() {
        return valueProperty;
    }

    public final long getValue() {
        return valueProperty.get();
    }

    public final void setValue(long value) {
        this.valueProperty.set(value);
    }

    public void refresh() {
        refresh(Config.get().getUnitFormat(), Config.get().getCoinUnit());
    }

    public void refresh(UnitFormat unitFormat, CoinUnit coinUnit) {
        setValueAsText(getValue(), unitFormat, coinUnit);
    }

    private void setValueAsText(Long value, UnitFormat unitFormat, CoinUnit coinUnit) {
        if(Config.get().isHideAmounts()) {
            setText(CoinLabel.HIDDEN_AMOUNT_TEXT);
            setTooltip(null);
            setContextMenu(null);
            return;
        }

        setTooltip(tooltip);
        setContextMenu(contextMenu);

        if(unitFormat == null) {
            unitFormat = UnitFormat.DOT;
        }

        this.coinUnit = CoinAmountText.resolve(value, coinUnit);
        tooltip.setText(CoinAmountText.tooltip(value, coinUnit, unitFormat));
        setText(CoinAmountText.display(value, coinUnit, unitFormat));
    }

    private class CoinContextMenu extends ContextMenu {
        public CoinContextMenu() {
            MenuItem copySatsValue = new MenuItem("Copy Value in tokens");
            copySatsValue.setOnAction(AE -> {
                hide();
                ClipboardContent content = new ClipboardContent();
                content.putString(Long.toString(getValue()));
                Clipboard.getSystemClipboard().setContent(content);
            });

            MenuItem copyBtcValue = new MenuItem("Copy Value in GFCN");
            copyBtcValue.setOnAction(AE -> {
                hide();
                ClipboardContent content = new ClipboardContent();
                UnitFormat format = Config.get().getUnitFormat() == null ? UnitFormat.DOT : Config.get().getUnitFormat();
                content.putString(format.formatCoinValue(getValue()));
                Clipboard.getSystemClipboard().setContent(content);
            });

            getItems().addAll(copySatsValue, copyBtcValue);
        }
    }
}
