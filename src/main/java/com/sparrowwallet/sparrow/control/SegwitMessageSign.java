package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.crypto.Bip322;
import com.sparrowwallet.drongo.crypto.ECKey;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletNode;

public final class SegwitMessageSign implements MessageSignStrategy {
    @Override
    public String sign(Wallet wallet, WalletNode walletNode, String message, MessageSignFormat format) throws Exception {
        Keystore keystore = wallet.getKeystores().getFirst();
        ECKey privKey = keystore.getKey(walletNode);
        ScriptType scriptType = walletNode.getAddress().getScriptType();
        if(format == MessageSignFormat.BIP322) {
            return Bip322.signMessageBip322(ScriptType.P2WPKH, message, privKey);
        }
        return privKey.signMessage(message, scriptType);
    }

    @Override
    public boolean verify(Address address, String message, String signature, Wallet wallet, WalletNode walletNode, MessageSignFormat format) throws Exception {
        ScriptType scriptType = address.getScriptType();
        if(scriptType == ScriptType.P2SH) {
            scriptType = ScriptType.P2SH_P2WPKH;
        }
        if(format == MessageSignFormat.BIP322) {
            return Bip322.verifyMessageBip322(ScriptType.P2WPKH, address, message, signature);
        }
        ECKey signedKey = ECKey.signedMessageToKey(message, signature, format == MessageSignFormat.ELECTRUM);
        signedKey.verifyMessage(message, signature);
        Address derived = scriptType.getAddress(com.sparrowwallet.drongo.policy.PolicyType.SINGLE_SEGWIT, signedKey);
        return address.equals(derived);
    }

    @Override
    public boolean showsFormatRow() {
        return true;
    }
}
