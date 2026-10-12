package com.sparrowwallet.sparrow.event;

import com.sparrowwallet.drongo.CoinUnit;
import com.sparrowwallet.drongo.wallet.BlockTransaction;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.control.CoinLabel;
import com.sparrowwallet.sparrow.io.Config;
import com.sparrowwallet.sparrow.wallet.Entry;
import com.sparrowwallet.sparrow.wallet.HashIndexEntry;
import com.sparrowwallet.sparrow.wallet.TransactionEntry;
import com.sparrowwallet.sparrow.wallet.TransactionHashIndexEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewWalletTransactionsEvent {
    private final Wallet wallet;
    private final List<TransactionEntry> transactionEntries;
    private final long totalBlockchainValue;
    private final long totalMempoolValue;

    public NewWalletTransactionsEvent(Wallet wallet, List<TransactionEntry> transactionEntries) {
        this.wallet = wallet;
        this.transactionEntries = transactionEntries;
        this.totalBlockchainValue = transactionEntries.stream().filter(txEntry -> txEntry.getConfirmations() > 0).mapToLong(Entry::getValue).sum();
        this.totalMempoolValue = transactionEntries.stream().filter(txEntry ->txEntry.getConfirmations() == 0).mapToLong(Entry::getValue).sum();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public List<BlockTransaction> getBlockTransactions() {
        return transactionEntries.stream().map(TransactionEntry::getBlockTransaction).collect(Collectors.toList());
    }

    public long getTotalValue() {
        return totalBlockchainValue + totalMempoolValue;
    }

    public long getTotalBlockchainValue() {
        return totalBlockchainValue;
    }

    public long getTotalMempoolValue() {
        return totalMempoolValue;
    }

    public String getValueAsText(long value) {
        if(Config.get().isHideAmounts()) {
            return CoinLabel.HIDDEN_AMOUNT_TEXT;
        }

        UnitFormat format = Config.get().getUnitFormat();
        if(format == null) {
            format = UnitFormat.DOT;
        }

        CoinUnit unit = Config.get().getCoinUnit();
        if(unit == null || unit.equals(CoinUnit.AUTO)) {
            unit = (value >= CoinUnit.getAutoThreshold() ? CoinUnit.GFCN : CoinUnit.TOKENS);
        }

        if(unit == CoinUnit.GFCN) {
            return format.formatCoinValue(value) + " GFCN";
        }

        return format.formatTokensValue(value) + " tokens";
    }

    public List<BlockTransaction> getUnspentConfirmingWhirlpoolMixTransactions() {
        List<BlockTransaction> mixTransactions = new ArrayList<>();
        if(wallet.isWhirlpoolMixWallet()) {
            return transactionEntries.stream()
                    .filter(txEntry -> txEntry.getValue() == 0 && txEntry.isConfirming() && !allOutputsSpent(txEntry))
                    .map(TransactionEntry::getBlockTransaction).collect(Collectors.toList());
        }

        return mixTransactions;
    }

    private boolean allOutputsSpent(TransactionEntry txEntry) {
        return txEntry.getChildren().stream()
                .map(refEntry -> ((TransactionHashIndexEntry)refEntry))
                .filter(txRefEntry -> txRefEntry.getType() == HashIndexEntry.Type.OUTPUT)
                .allMatch(txRefEntry -> txRefEntry.getHashIndex().isSpent());
    }
}
