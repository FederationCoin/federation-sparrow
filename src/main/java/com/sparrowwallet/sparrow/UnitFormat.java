package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.protocol.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public enum UnitFormat {
    DOT {
        private final DecimalFormat coinFormat = new DecimalFormat("0", getDecimalFormatSymbols());
        private final DecimalFormat tokensFormat = new DecimalFormat("#,##0", getDecimalFormatSymbols());
        private final DecimalFormat tableCoinFormat = new DecimalFormat("0.00000000", getDecimalFormatSymbols());
        private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00", getDecimalFormatSymbols());
        private final DecimalFormat tableCurrencyFormat = new DecimalFormat("0.00", getDecimalFormatSymbols());

        public DecimalFormat getCoinFormat() {
            coinFormat.setMaximumFractionDigits(8);
            return coinFormat;
        }

        public DecimalFormat getTokensFormat() {
            return tokensFormat;
        }

        public DecimalFormat getTableCoinFormat() {
            return tableCoinFormat;
        }

        public DecimalFormat getCurrencyFormat() {
            return currencyFormat;
        }

        public DecimalFormat getTableCurrencyFormat() {
            return tableCurrencyFormat;
        }

        public DecimalFormatSymbols getDecimalFormatSymbols() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            return symbols;
        }
    },
    COMMA {
        private final DecimalFormat coinFormat = new DecimalFormat("0", getDecimalFormatSymbols());
        private final DecimalFormat tokensFormat = new DecimalFormat("#,##0", getDecimalFormatSymbols());
        private final DecimalFormat tableCoinFormat = new DecimalFormat("0.00000000", getDecimalFormatSymbols());
        private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00", getDecimalFormatSymbols());
        private final DecimalFormat tableCurrencyFormat = new DecimalFormat("0.00", getDecimalFormatSymbols());

        public DecimalFormat getCoinFormat() {
            coinFormat.setMaximumFractionDigits(8);
            return coinFormat;
        }

        public DecimalFormat getTokensFormat() {
            return tokensFormat;
        }

        public DecimalFormat getTableCoinFormat() {
            return tableCoinFormat;
        }

        public DecimalFormat getCurrencyFormat() {
            return currencyFormat;
        }

        public DecimalFormat getTableCurrencyFormat() {
            return tableCurrencyFormat;
        }

        public DecimalFormatSymbols getDecimalFormatSymbols() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
            return symbols;
        }
    };

    public abstract DecimalFormatSymbols getDecimalFormatSymbols();

    public abstract DecimalFormat getCoinFormat();

    public abstract DecimalFormat getTokensFormat();

    public abstract DecimalFormat getTableCoinFormat();

    public abstract DecimalFormat getCurrencyFormat();

    public abstract DecimalFormat getTableCurrencyFormat();

    public String formatCoinValue(Long amount) {
        return getCoinFormat().format(amount.doubleValue() / Transaction.TOKENS_PER_COIN);
    }

    public String tableFormatCoinValue(Long amount) {
        return getTableCoinFormat().format(amount.doubleValue() / Transaction.TOKENS_PER_COIN);
    }

    public String formatTokensValue(Long amount) {
        return getTokensFormat().format(amount);
    }

    public String formatCurrencyValue(double amount) {
        return getCurrencyFormat().format(amount);
    }

    public String tableFormatCurrencyValue(double amount) {
        return getTableCurrencyFormat().format(amount);
    }

    public String getGroupingSeparator() {
        return Character.toString(getDecimalFormatSymbols().getGroupingSeparator());
    }

    public String getDecimalSeparator() {
        return Character.toString(getDecimalFormatSymbols().getDecimalSeparator());
    }
}
