package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletNode;

import java.nio.charset.StandardCharsets;

public final class MlDsa44MessageSign implements MessageSignStrategy {
    @Override
    public String sign(Wallet wallet, WalletNode walletNode, String message, MessageSignFormat format) throws Exception {
        Keystore keystore = wallet.getKeystores().stream().filter(Keystore::hasPrivateKey).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Dilithium 44 key to sign with"));
        MlDsa44.Keypair key = keystore.getMlDsaKeypair(walletNode);
        byte[] signature = MlDsa44.sign(key.secret(), message.getBytes(StandardCharsets.UTF_8));
        return Utils.bytesToHex(key.pubkey()) + Utils.bytesToHex(signature);
    }

    @Override
    public boolean verify(Address address, String message, String signature, Wallet wallet, WalletNode walletNode, MessageSignFormat format) throws Exception {
        byte[] blob = Utils.hexToBytes(signature.trim());
        byte[] pubkey;
        byte[] sig;
        if(blob.length == MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE) {
            pubkey = java.util.Arrays.copyOfRange(blob, 0, MlDsa44.PUBLIC_KEY_SIZE);
            sig = java.util.Arrays.copyOfRange(blob, MlDsa44.PUBLIC_KEY_SIZE, blob.length);
        } else if(wallet != null && !wallet.getKeystores().isEmpty() && walletNode != null) {
            pubkey = wallet.getKeystores().stream().filter(Keystore::hasPrivateKey).findFirst()
                    .orElse(wallet.getKeystores().get(0)).getMlDsaKeypair(walletNode).pubkey();
            sig = blob;
        } else {
            throw new IllegalArgumentException("Could not verify: this address's public key is not available.");
        }
        if(address.getData().length == 32 && !java.util.Arrays.equals(MlDsa44.keyHash(pubkey), address.getData())) {
            return false;
        }
        return MlDsa44.verify(pubkey, sig, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean showsFormatRow() {
        return false;
    }
}
