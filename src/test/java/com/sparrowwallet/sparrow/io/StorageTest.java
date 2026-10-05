package com.sparrowwallet.sparrow.io;

import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.address.MlDsaAddress;
import com.sparrowwallet.drongo.crypto.Argon2KeyDeriver;
import com.sparrowwallet.drongo.policy.Policy;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.wallet.DeterministicSeed;
import com.sparrowwallet.drongo.wallet.InvalidWalletException;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.KeystoreSource;
import com.sparrowwallet.drongo.wallet.MnemonicException;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class StorageTest extends IoTest {
    private Wallet createMlDsaSeedWallet(String name) throws MnemonicException {
        byte[] entropy = new byte[16];
        Arrays.fill(entropy, (byte)0x21);
        DeterministicSeed seed = new DeterministicSeed(entropy, "", 0L);
        Keystore keystore = Keystore.fromSeed(seed, PolicyType.SINGLE_HD, ScriptType.MLDSA_SINGLE.getDefaultDerivation());
        Wallet wallet = new Wallet(name);
        wallet.setPolicyType(PolicyType.SINGLE_HD);
        wallet.setScriptType(ScriptType.MLDSA_SINGLE);
        wallet.getKeystores().add(keystore);
        wallet.setDefaultPolicy(Policy.getPolicy(wallet.getPolicyType(), wallet.getScriptType(), wallet.getKeystores(), 1));
        return wallet;
    }

    private Wallet createMlDsaWatchWallet(String name) throws MnemonicException {
        Wallet seedWallet = createMlDsaSeedWallet(name);
        Keystore fromSeed = seedWallet.getKeystores().get(0);
        Keystore watch = new Keystore("Keystore 1");
        watch.setSource(KeystoreSource.SW_WATCH);
        watch.setWalletModel(WalletModel.SPARROW);
        watch.setKeyDerivation(fromSeed.getKeyDerivation());
        watch.setExtendedPublicKey(fromSeed.getExtendedPublicKey());
        Wallet wallet = new Wallet(name);
        wallet.setPolicyType(PolicyType.SINGLE_HD);
        wallet.setScriptType(ScriptType.MLDSA_SINGLE);
        wallet.getKeystores().add(watch);
        wallet.setDefaultPolicy(Policy.getPolicy(wallet.getPolicyType(), wallet.getScriptType(), wallet.getKeystores(), 1));
        return wallet;
    }

    private void assertValid(Wallet wallet) {
        try {
            wallet.checkWallet();
        } catch(InvalidWalletException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }

    private void rederiveSeedXpubs(Wallet wallet, Wallet decryptedCopy) throws MnemonicException {
        for(int i = 0; i < wallet.getKeystores().size(); i++) {
            Keystore keystore = wallet.getKeystores().get(i);
            if(keystore.hasSeed()) {
                Keystore copyKeystore = decryptedCopy.getKeystores().get(i);
                Keystore derivedKeystore = Keystore.fromSeed(copyKeystore.getSeed(), wallet.getPolicyType(), copyKeystore.getKeyDerivation().getDerivation());
                keystore.setKeyDerivation(derivedKeystore.getKeyDerivation());
                keystore.setExtendedPublicKey(derivedKeystore.getExtendedPublicKey());
                keystore.getSeed().setPassphrase(copyKeystore.getSeed().getPassphrase());
                copyKeystore.getSeed().clear();
            }
        }
    }

    private Storage saveMlDsaWallet(Wallet wallet, CharSequence password) throws Exception {
        Path dir = Files.createTempDirectory("sprw-storage");
        dir.toFile().deleteOnExit();
        File tempWallet = dir.resolve(wallet.getName() + ".mv.db").toFile();
        Storage storage = new Storage(PersistenceType.DB, tempWallet);
        storage.setKeyDeriver(new Argon2KeyDeriver());
        storage.setEncryptionPubKey(password == null ? Storage.NO_PASSWORD_KEY : com.sparrowwallet.drongo.crypto.ECKey.fromPublicOnly(storage.getKeyDeriver().deriveECKey(password)));
        storage.saveWallet(wallet);
        return storage;
    }

    @Test
    public void loadWallet() throws Exception {
        Storage storage = saveMlDsaWallet(createMlDsaWatchWallet("testd"), null);
        try {
            Wallet wallet = storage.loadUnencryptedWallet().getWallet();
            assertValid(wallet);
            Assertions.assertEquals(ScriptType.MLDSA_SINGLE, wallet.getScriptType());
        } finally {
            storage.closeAndWait();
        }
    }

    @Test
    public void loadSeedWallet() throws Exception {
        Storage saved = saveMlDsaWallet(createMlDsaSeedWallet("testd2"), "pass");
        File walletFile = saved.getWalletFile();
        saved.closeAndWait();

        Storage storage = new Storage(PersistenceType.DB, walletFile);
        try {
            WalletAndKey walletAndKey = storage.loadEncryptedWallet("pass");
            Wallet wallet = walletAndKey.getWallet();
            Wallet copy = wallet.copy();
            copy.decrypt(walletAndKey.getKey());
            rederiveSeedXpubs(wallet, copy);

            assertValid(wallet);
            Assertions.assertEquals("testd2", wallet.getName());
            Assertions.assertEquals(PolicyType.SINGLE_HD, wallet.getPolicyType());
            Assertions.assertEquals(ScriptType.MLDSA_SINGLE, wallet.getScriptType());
            Assertions.assertEquals(1, wallet.getDefaultPolicy().getNumSignaturesRequired());
            Assertions.assertTrue(wallet.getKeystores().get(0).hasSeed());
            Assertions.assertInstanceOf(MlDsaAddress.class, wallet.getFreshNode(KeyPurpose.RECEIVE).getAddress());
        } finally {
            storage.closeAndWait();
        }
    }

    @Test
    public void multipleLoadTest() throws Exception {
        for(int i = 0; i < 5; i++) {
            loadSeedWallet();
        }
    }

    @Test
    public void saveWallet() throws Exception {
        Storage storage = saveMlDsaWallet(createMlDsaWatchWallet("testd"), "pass");
        File walletFile = storage.getWalletFile();
        storage.closeAndWait();

        Storage loaded = new Storage(PersistenceType.DB, walletFile);
        try {
            Wallet wallet = loaded.loadEncryptedWallet("pass").getWallet();
            assertValid(wallet);
            Assertions.assertEquals(ScriptType.MLDSA_SINGLE, wallet.getScriptType());

            Path dir = Files.createTempDirectory("sprw-storage-copy");
            dir.toFile().deleteOnExit();
            File tempWallet = dir.resolve("testd.mv.db").toFile();
            Storage tempStorage = new Storage(PersistenceType.DB, tempWallet);
            tempStorage.setKeyDeriver(loaded.getKeyDeriver());
            tempStorage.setEncryptionPubKey(loaded.getEncryptionPubKey());
            tempStorage.saveWallet(wallet);
            tempStorage.closeAndWait();

            Storage temp2Storage = new Storage(PersistenceType.DB, tempWallet);
            try {
                wallet = temp2Storage.loadEncryptedWallet("pass").getWallet();
                assertValid(wallet);
                Assertions.assertEquals(ScriptType.MLDSA_SINGLE, wallet.getScriptType());
            } finally {
                temp2Storage.closeAndWait();
            }
        } finally {
            loaded.closeAndWait();
        }
    }

    @Test
    public void getBackupsExcludesLongerWalletNames() throws IOException {
        File backupDir = createBackupDir("Savings-20250101120000.mv.db", "Savings-20240101120000.mv.db",
                "Savings-2023-20250101120000.mv.db", "Savings.old-20250101120000.mv.db", "SavingsX-20250101120000.mv.db");

        assertBackups(backupDir, PersistenceType.DB, "Savings.mv.db", "Savings-20250101120000.mv.db", "Savings-20240101120000.mv.db");
        assertBackups(backupDir, PersistenceType.DB, "Savings-2023.mv.db", "Savings-2023-20250101120000.mv.db");
        assertBackups(backupDir, PersistenceType.DB, "Savings.old.mv.db", "Savings.old-20250101120000.mv.db");
    }

    @Test
    public void getBackupsRequiresAWholeDateAndAMatchingExtension() throws IOException {
        File backupDir = createBackupDir("Savings-20250101120000.mv.db", "Savings-2025010112000.mv.db", "Savings-202501011200000.mv.db",
                "Savings-20250101120000.json", "Savings-20250101120000", "Savings.mv.db", "Savings-notes.txt");

        assertBackups(backupDir, PersistenceType.DB, "Savings.mv.db", "Savings-20250101120000.mv.db");
        assertBackups(backupDir, PersistenceType.JSON, "Savings.json", "Savings-20250101120000.json");
        assertBackups(backupDir, PersistenceType.JSON, "Savings", "Savings-20250101120000");
    }

    @Test
    public void getBackupsTreatsAWalletNameLiterally() throws IOException {
        File backupDir = createBackupDir("SavingsXold-20250101120000.mv.db");

        assertBackups(backupDir, PersistenceType.DB, "Savings.old.mv.db");
    }

    private File createBackupDir(String... backupNames) throws IOException {
        Path backupDir = Files.createTempDirectory("sprw-backup");
        backupDir.toFile().deleteOnExit();
        for(String backupName : backupNames) {
            File backup = backupDir.resolve(backupName).toFile();
            backup.createNewFile();
            backup.deleteOnExit();
        }

        return backupDir.toFile();
    }

    private void assertBackups(File backupDir, PersistenceType persistenceType, String walletFileName, String... expectedBackupNames) {
        Storage storage = new Storage(persistenceType, new File(backupDir.getParentFile(), walletFileName));
        File[] backups = storage.getBackups(backupDir, null);
        Assertions.assertArrayEquals(expectedBackupNames, Arrays.stream(backups).map(File::getName).toArray(String[]::new));
    }

    @AfterEach
    void tearDown() {
        System.setProperty(Wallet.ALLOW_DERIVATIONS_MATCHING_OTHER_NETWORKS_PROPERTY, "false");
    }
}
