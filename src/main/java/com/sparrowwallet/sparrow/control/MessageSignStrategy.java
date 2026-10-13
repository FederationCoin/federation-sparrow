package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletNode;

public interface MessageSignStrategy {
    String sign(Wallet wallet, WalletNode walletNode, String message, MessageSignFormat format) throws Exception;

    boolean verify(Address address, String message, String signature, Wallet wallet, WalletNode walletNode, MessageSignFormat format) throws Exception;

    boolean showsFormatRow();

    static MessageSignStrategy forAddress(Address address) {
        return switch(address.getScriptType()) {
            case MLDSA87_SINGLE, MLDSA87_MULTI -> new MlDsa87MessageSign();
            case MLDSA_SINGLE, MLDSA_MULTI -> new MlDsa44MessageSign();
            case P2WPKH, P2SH_P2WPKH -> new SegwitMessageSign();
            default -> throw new IllegalArgumentException("Message signing is not available for this address.");
        };
    }
}
