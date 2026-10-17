package com.sparrowwallet.sparrow.event;

import com.sparrowwallet.drongo.uri.CoinURI;
import com.sparrowwallet.drongo.wallet.Payment;
import com.sparrowwallet.drongo.wallet.Wallet;

import java.util.List;

public class SendPaymentsEvent {
    private final Wallet wallet;
    private final List<Payment> payments;
    private final CoinURI coinURI;

    public SendPaymentsEvent(Wallet wallet, List<Payment> payments) {
        this(wallet, payments, null);
    }

    public SendPaymentsEvent(Wallet wallet, List<Payment> payments, CoinURI coinURI) {
        this.wallet = wallet;
        this.payments = payments;
        this.coinURI = coinURI;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public CoinURI getCoinURI() {
        return coinURI;
    }
}
