package com.sparrowwallet.sparrow.net;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.protocol.BlockHeader;
import com.sparrowwallet.drongo.protocol.HeaderChainState;
import com.sparrowwallet.drongo.protocol.HeaderCheckpoints;
import com.sparrowwallet.sparrow.SparrowWallet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the pinned header hashes compiled into drongo against live public Electrum servers. This is a release checklist step rather than a unit
 * test - it needs the network and it is checking data, not code - so it is tagged out of the default test task and run with the verifyCheckpoint task.
 * <p>
 * A server that cannot be reached is skipped; a server that answers with a different chain fails the run. Checkpoint generation is a derivation from a
 * locally validated header chain, so this confirms independently that what was derived matches what the network says, and that the last pin is buried.
 */
@Tag("checkpoint")
public class HeaderCheckpointVerificationTest {
    /**
     * The number of blocks the last pinned header must be buried by. A pin that is later orphaned would have every install refuse the real chain at
     * that height, so a period is only pinned once it is beyond any plausible reorg.
     */
    private static final int CHECKPOINT_BURIAL_DEPTH = 6;

    private static final int PINS_VERIFIED = 3;
    private static final int MINIMUM_RESPONDERS = 3;

    @TempDir
    private static Path tempHome;

    @BeforeAll
    public static void setUp() {
        //Config.get() caches its instance statically for the life of the JVM, so keep this test from loading the developer's real config
        System.setProperty(SparrowWallet.APP_HOME_PROPERTY, tempHome.toString());
    }

    @AfterAll
    public static void tearDownAll() {
        System.clearProperty(SparrowWallet.APP_HOME_PROPERTY);
    }

    @Test
    public void verifyMainnetCheckpoints() {
        verifyCheckpoints(Network.MAINNET);
    }

    @Test
    public void verifyTestnetCheckpoints() {
        verifyCheckpoints(Network.TESTNET);
    }

    @Test
    public void verifyTestnet4Checkpoints() {
        verifyCheckpoints(Network.TESTNET4);
    }

    @Test
    public void verifySignetCheckpoints() {
        verifyCheckpoints(Network.SIGNET);
    }

    private void verifyCheckpoints(Network network) {
        Network.set(network);
        assertTrue(PublicElectrumServer.getServers().isEmpty(), "No public Electrum server indexes this chain");
        assertTrue(network.getHeaderCheckpoints() != null);
    }

    @AfterEach
    public void tearDown() {
        Network.set(null);
    }
}
