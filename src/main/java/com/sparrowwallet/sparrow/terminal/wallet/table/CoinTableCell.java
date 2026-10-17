package com.sparrowwallet.sparrow.terminal.wallet.table;

import com.google.common.base.Strings;
import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.io.Config;
import com.sparrowwallet.sparrow.wallet.Entry;
import com.sparrowwallet.sparrow.wallet.NodeEntry;
import com.sparrowwallet.sparrow.wallet.TransactionEntry;

public class CoinTableCell extends TableCell {
    public static final int TRANSACTION_WIDTH = 24;
    public static final int UTXO_WIDTH = 18;

    private final boolean balance;

    public CoinTableCell(Entry entry, boolean balance) {
        super(entry);
        this.balance = balance;
    }

    @Override
    public String formatCell() {
        Long value = null;
        if(balance && entry instanceof TransactionEntry transactionEntry) {
            value = transactionEntry.getBalance();
        } else {
            value = entry.getValue();
        }

        if(value == null) {
            value = 0L;
        }

        CoinUnit unit = Config.get().getCoinUnit();
        if(unit == null || unit.equals(CoinUnit.AUTO)) {
            unit = (value >= CoinUnit.getAutoThreshold() ? CoinUnit.GFCN : CoinUnit.TOKENS);
        }

        UnitFormat format = Config.get().getUnitFormat();
        if(format == null) {
            format = UnitFormat.DOT;
        }

        String formattedValue = unit == CoinUnit.TOKENS ? format.formatTokensValue(value) : format.formatCoinValue(value);
        return Strings.padStart(formattedValue, getWidth(entry), ' ');
    }

    private int getWidth(Entry entry) {
        return entry instanceof TransactionEntry ? TRANSACTION_WIDTH : UTXO_WIDTH;
    }
}
