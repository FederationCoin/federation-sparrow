package com.sparrowwallet.sparrow.io;

import com.sparrowwallet.drongo.crypto.ChildNumber;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.wallet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Bip39 implements KeystoreMnemonicImport {
    private static final Logger log = LoggerFactory.getLogger(Bip39.class);
    @Override
    public String getName() {
        return "Mnemonic Words (BIP39)";
    }

    @Override
    public WalletModel getWalletModel() {
        return WalletModel.SEED;
    }

    @Override
    public String getKeystoreImportDescription() {
        return "Generate or import your 12 to 24 word mnemonic and optional passphrase.";
    }

    @Override
    public Keystore getKeystore(PolicyType policyType, List<ChildNumber> derivation, List<String> mnemonicWords, String passphrase) throws ImportException {
        try {
            Bip39MnemonicCode.INSTANCE.check(mnemonicWords);
            DeterministicSeed seed = new DeterministicSeed(mnemonicWords, passphrase, System.currentTimeMillis(), DeterministicSeed.Type.BIP39);
            return Keystore.fromSeed(seed, policyType, derivation);
        } catch (Exception e) {
            try {
                ElectrumMnemonicCode.INSTANCE.check(mnemonicWords);
                throw new ImportException(new MnemonicException.MnemonicTypeException(DeterministicSeed.Type.ELECTRUM));
            } catch(Exception ex) {
                if(ex instanceof ImportException && ex.getCause() instanceof MnemonicException.MnemonicTypeException) {
                    throw (ImportException)ex;
                }
                log.warn("Mnemonic is not Electrum either", ex);
            }

            throw new ImportException(e);
        }
    }
}
