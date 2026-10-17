package com.sparrowwallet.sparrow.net;

import com.google.common.net.HostAndPort;
import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.sparrow.io.Server;

import java.util.List;

/**
 * Public Electrum hosts for this chain. None yet: no public server indexes it.
 * Connect a node, or a private Electrum server that does.
 */
public enum PublicElectrumServer {
    ;

    public static final List<Network> SUPPORTED_NETWORKS = List.of();

    public static List<PublicElectrumServer> getServers() {
        return List.of();
    }

    public static boolean supportedNetwork() {
        return SUPPORTED_NETWORKS.contains(Network.get());
    }

    public static PublicElectrumServer fromServer(Server server) {
        return null;
    }

    public static boolean isPublicServer(HostAndPort hostAndPort) {
        return false;
    }

    public Server getServer() {
        throw new IllegalStateException("no public Electrum server");
    }
}
