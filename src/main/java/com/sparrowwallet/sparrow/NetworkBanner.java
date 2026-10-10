package com.sparrowwallet.sparrow;

import com.sparrowwallet.drongo.Network;

/**
 * Tab-bar network copy. Dummy MAIN is not a live chain.
 */
public final class NetworkBanner {
    private NetworkBanner() {
    }

    public static String text(Network network) {
        return network == Network.MAINNET ? "Main is not live" : network.toDisplayString();
    }
}
