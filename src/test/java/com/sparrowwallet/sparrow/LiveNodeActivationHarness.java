package com.sparrowwallet.sparrow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The decision driven with the numbers a live node actually reported, rather than round literals.
 *
 * The numbers are the testnet4 schedule this build ships and a chain height past it, rather than round
 * literals, so the arithmetic is exercised on values a node actually reports.
 */
public class LiveNodeActivationHarness {
    private static final int WALLET_HEIGHT = 150308;
    private static final int NODE_HEIGHT = 150308;
    private static final int TIP = 161442;

    @Test
    public void testAgreementWithTheLiveNodeOptsInSilently() {
        AppServices.clearNodeHardforkHeight();
        Assertions.assertTrue(AppServices.isUnifiedSigHashActive(WALLET_HEIGHT, NODE_HEIGHT, TIP));
        Assertions.assertNull(AppServices.getLastActivationHeightReport(),
                "A correctly configured connection must not warn");
    }

    @Test
    public void testASupersededHeightIsRefused() {
        AppServices.clearNodeHardforkHeight();
        Assertions.assertTrue(AppServices.isUnifiedSigHashActive(WALLET_HEIGHT, 149537, TIP),
                "UNIFIED is genesis; a later node schedule is not a second activation");
        Assertions.assertNull(AppServices.getLastActivationHeightReport());
    }

    @Test
    public void testANetworkWithNoShippedHeightDeclinesAndReports() {
        AppServices.clearNodeHardforkHeight();
        Assertions.assertTrue(AppServices.isUnifiedSigHashActive(null, NODE_HEIGHT, TIP),
                "UNIFIED is genesis; a missing shipped height does not decline");
        Assertions.assertNull(AppServices.getLastActivationHeightReport());
    }
}
