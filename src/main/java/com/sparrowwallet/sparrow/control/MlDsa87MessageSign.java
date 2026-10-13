package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.crypto.MlDsa87;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletNode;

import java.nio.charset.StandardCharsets;

public final class MlDsa87MessageSign implements MessageSignStrategy {
    @Override
    public String sign(Wallet wallet, WalletNode walletNode, String message, MessageSignFormat format) throws Exception {
        Keystore keystore = wallet.getKeystores().getFirst();
        MlDsa87.Keypair key = keystore.getMlDsa87Keypair(walletNode);
        return Utils.bytesToHex(MlDsa87.sign(key.secret(), message.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public boolean verify(Address address, String message, String signature, Wallet wallet, WalletNode walletNode, MessageSignFormat format) throws Exception {
        if(wallet == null || wallet.getKeystores().isEmpty() || walletNode == null) {
            throw new IllegalArgumentException("Could not verify: this address's public key is not available.");
        }
        byte[] pubkey = wallet.getKeystores().getFirst().getMlDsa87Keypair(walletNode).pubkey();
        return MlDsa87.verify(pubkey, Utils.hexToBytes(signature.trim()), message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean showsFormatRow() {
        return false;
    }
}
