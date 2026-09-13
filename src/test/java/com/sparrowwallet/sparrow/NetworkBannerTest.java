package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.Network;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkBannerTest {
    @Test
    public void dummyMainIsNotLive() {
        Assertions.assertEquals("Main is not live", NetworkBanner.text(Network.MAINNET));
    }

    @Test
    public void otherNetworksUseDisplayName() {
        Assertions.assertEquals(Network.TESTNET.toDisplayString(), NetworkBanner.text(Network.TESTNET));
        Assertions.assertEquals(Network.REGTEST.toDisplayString(), NetworkBanner.text(Network.REGTEST));
        Assertions.assertEquals(Network.SIGNET.toDisplayString(), NetworkBanner.text(Network.SIGNET));
        Assertions.assertEquals(Network.TESTNET4.toDisplayString(), NetworkBanner.text(Network.TESTNET4));
    }
}
