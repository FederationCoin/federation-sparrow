package com.sparrowwallet.sparrow.keystoreimport;

import com.sparrowwallet.drongo.wallet.KeystoreSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HardwareSourcesTest {
    @Test
    public void hardwareSourcesOffUntilAManufacturerSupportsUs() {
        Assertions.assertFalse(KeystoreImportDialog.getSupportedSources().contains(KeystoreSource.HW_USB));
        Assertions.assertFalse(KeystoreImportDialog.getSupportedSources().contains(KeystoreSource.HW_AIRGAPPED));
        Assertions.assertTrue(KeystoreImportDialog.getSupportedSources().contains(KeystoreSource.SW_SEED));
        Assertions.assertTrue(KeystoreImportDialog.getSupportedSources().contains(KeystoreSource.SW_WATCH));
    }
}
