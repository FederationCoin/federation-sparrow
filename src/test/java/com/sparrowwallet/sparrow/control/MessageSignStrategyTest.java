package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.address.MlDsa44Address;
import com.sparrowwallet.drongo.address.MlDsa87Address;
import com.sparrowwallet.drongo.address.P2WPKHAddress;
import com.sparrowwallet.drongo.crypto.MlDsa87;
import com.sparrowwallet.drongo.policy.Policy;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.wallet.DeterministicSeed;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageSignStrategyTest {
    @BeforeEach
    public void setNetwork() {
        Network.set(Network.TESTNET);
    }

    @AfterEach
    public void clearNetwork() {
        Network.set(null);
    }

    @Test
    public void offeredKindsHaveAStrategy() {
        Assertions.assertInstanceOf(MlDsa87MessageSign.class,
                MessageSignStrategy.forAddress(new MlDsa87Address(new byte[32])));
        Assertions.assertInstanceOf(MlDsa44MessageSign.class,
                MessageSignStrategy.forAddress(new MlDsa44Address(new byte[32])));
        Assertions.assertInstanceOf(SegwitMessageSign.class,
                MessageSignStrategy.forAddress(new P2WPKHAddress(new byte[20])));
        Assertions.assertFalse(MessageSignStrategy.forAddress(new MlDsa87Address(new byte[32])).showsFormatRow());
        Assertions.assertTrue(MessageSignStrategy.forAddress(new P2WPKHAddress(new byte[20])).showsFormatRow());
    }

    @Test
    public void dilithium87RoundTrip() throws Exception {
        DeterministicSeed seed = new DeterministicSeed(
                "absent essay fox snake vast pumpkin height crouch silent bulb excuse razor",
                "", 0, DeterministicSeed.Type.BIP39);
        Wallet wallet = new Wallet();
        wallet.setPolicyType(PolicyType.SINGLE_MLDSA87);
        wallet.setScriptType(ScriptType.MLDSA87_SINGLE);
        Keystore keystore = Keystore.fromSeed(seed, PolicyType.SINGLE_MLDSA87, ScriptType.MLDSA87_SINGLE.getDefaultDerivation());
        wallet.getKeystores().add(keystore);
        wallet.setDefaultPolicy(Policy.getPolicy(PolicyType.SINGLE_MLDSA87, ScriptType.MLDSA87_SINGLE, wallet.getKeystores(), 1));
        WalletNode node = wallet.getFreshNode(com.sparrowwallet.drongo.KeyPurpose.RECEIVE);
        MessageSignStrategy strategy = MessageSignStrategy.forAddress(node.getAddress());
        String signature = strategy.sign(wallet, node, "hello", MessageSignFormat.ELECTRUM);
        Assertions.assertTrue(strategy.verify(node.getAddress(), "hello", signature, wallet, node, MessageSignFormat.ELECTRUM));
        MlDsa87.Keypair key = keystore.getMlDsa87Keypair(node);
        Assertions.assertTrue(MlDsa87.verify(key.pubkey(),
                com.sparrowwallet.drongo.Utils.hexToBytes(signature),
                "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
