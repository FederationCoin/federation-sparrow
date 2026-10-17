package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.wallet.WalletModel;
import com.sparrowwallet.sparrow.control.PrivateKeySweepDialog;
import com.sparrowwallet.sparrow.payjoin.Payjoin;
import com.sparrowwallet.sparrow.paynym.PayNym;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class OfferedFeatureTest {
    @Test
    public void payNymAndPayjoinStayOfferedOff() {
        Assertions.assertFalse(PayNym.supportedOnThisChain());
        Assertions.assertFalse(Payjoin.supportedOnThisChain());
    }

    @Test
    public void sweepFollowsOfferedSegwit() {
        Assertions.assertEquals(PolicyType.offeredForNewWallets().contains(PolicyType.SINGLE_SEGWIT),
                PrivateKeySweepDialog.supportedOnThisChain());
        Assertions.assertTrue(PrivateKeySweepDialog.supportedOnThisChain());
    }

    @Test
    public void hardwareModelsStayOffThisChain() {
        Assertions.assertTrue(Arrays.stream(WalletModel.values())
                .filter(WalletModel::hasUsb)
                .noneMatch(WalletModel::supportsThisChain));
        Assertions.assertTrue(Arrays.stream(WalletModel.values())
                .filter(WalletModel::isAirgapped)
                .noneMatch(WalletModel::supportsThisChain));
        Assertions.assertTrue(WalletModel.SEED.supportsThisChain());
    }
}
