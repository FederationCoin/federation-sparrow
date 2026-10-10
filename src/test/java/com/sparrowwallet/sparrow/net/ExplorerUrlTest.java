package com.sparrowwallet.sparrow.net;

import com.sparrowwallet.drongo.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExplorerUrlTest {
    @AfterEach
    public void restoreNetwork() {
        Network.set(Network.MAINNET);
    }

    @Test
    public void mempoolApiUrlForks() {
        Assertions.assertEquals("https://mempool.federationcoin.org/api/",
                FeeRatesSource.mempoolFederationApiUrl(Network.TESTNET));
        Assertions.assertEquals("https://mempool.federationcoin.org/api/",
                FeeRatesSource.mempoolFederationApiUrl(Network.MAINNET));
        Assertions.assertEquals("https://mempool.federationcoin.org/signet/api/",
                FeeRatesSource.mempoolFederationApiUrl(Network.SIGNET));
        Assertions.assertEquals("https://mempool.federationcoin.org/testnet4/api/",
                FeeRatesSource.mempoolFederationApiUrl(Network.TESTNET4));
        Assertions.assertEquals("https://mempool.federationcoin.org/api/",
                FeeRatesSource.mempoolFederationApiUrl(Network.REGTEST));
    }

    @Test
    public void mempoolBroadcastPathForks() {
        Assertions.assertEquals("/api/tx", BroadcastSource.mempoolBroadcastTxPath(Network.TESTNET));
        Assertions.assertEquals("/api/tx", BroadcastSource.mempoolBroadcastTxPath(Network.MAINNET));
        Assertions.assertEquals("/signet/api/tx", BroadcastSource.mempoolBroadcastTxPath(Network.SIGNET));
        Assertions.assertEquals("/testnet4/api/tx", BroadcastSource.mempoolBroadcastTxPath(Network.TESTNET4));
        Assertions.assertThrows(IllegalStateException.class, () -> BroadcastSource.mempoolBroadcastTxPath(Network.REGTEST));
    }
}
