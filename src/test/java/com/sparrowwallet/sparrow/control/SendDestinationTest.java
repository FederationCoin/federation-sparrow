package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.address.P2WPKHAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SendDestinationTest {
    @BeforeEach
    public void setNetwork() {
        Network.set(Network.MAINNET);
    }

    @AfterEach
    public void clearNetwork() {
        Network.set(null);
    }
    @Test
    public void chainAddressToPayment() throws Exception {
        P2WPKHAddress address = new P2WPKHAddress(new byte[20]);
        SendDestination dest = new SendDestination.ChainAddress(address);
        Assertions.assertFalse(dest.needsDnsResolution());
        Assertions.assertEquals(address.toString(), dest.toString());
        Assertions.assertEquals(address, dest.toPayment("label", 12, false).getAddress());
    }

    @Test
    public void dnsHrnNeedsResolution() {
        SendDestination dest = new SendDestination.DnsHrn("example.gfcn");
        Assertions.assertTrue(dest.needsDnsResolution());
        Assertions.assertEquals("example.gfcn", dest.toString());
    }

    @Test
    public void silentPaymentParseFailsWithActionError() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> SendDestination.parse("gfcnsp1qqgste7k9hx0qftg6qmwlkqtwuy6cycyavzmzj85c6qdfhjdpdjtdgqjuexzk6murw56suy3e0rd2cgqvycxttddwsvgxe2usfpxumr70xcutr9q0"));
        Assertions.assertEquals(SendDestination.SILENT_PAYMENTS_UNAVAILABLE, ex.getMessage());
        Assertions.assertNull(SendDestination.parse("bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"));
    }
}
