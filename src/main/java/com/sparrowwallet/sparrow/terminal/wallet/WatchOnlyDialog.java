package com.sparrowwallet.sparrow.terminal.wallet;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.policy.Policy;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.KeystoreSource;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.drongo.wallet.WalletModel;
import com.sparrowwallet.drongo.wallet.WalletNode;
import com.sparrowwallet.sparrow.io.ImportException;
import com.sparrowwallet.sparrow.terminal.SparrowTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WatchOnlyDialog extends NewWalletDialog {
    private static final Logger log = LoggerFactory.getLogger(WatchOnlyDialog.class);

    private final TextBox descriptor;
    private final Button importWallet;

    public WatchOnlyDialog(String walletName) {
        super("Create Watch Only Wallet - " + walletName, walletName);

        setHints(List.of(Hint.CENTERED));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new GridLayout(2).setVerticalSpacing(0));

        mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
        mainPanel.addComponent(new EmptySpace(TerminalSize.ZERO));

        TerminalSize screenSize = SparrowTerminal.get().getScreen().getTerminalSize();
        int descriptorWidth = Math.min(Math.max(20, screenSize.getColumns() - 20), 120);

        mainPanel.addComponent(new Label("ML-DSA-44 address (xpub and Bitcoin descriptors are not a spend)"));
        mainPanel.addComponent(new EmptySpace(TerminalSize.ZERO));

        descriptor = new TextBox(new TerminalSize(descriptorWidth, 10));
        mainPanel.addComponent(descriptor);
        mainPanel.addComponent(new EmptySpace(TerminalSize.ZERO));

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
        buttonPanel.addComponent(new Button("Cancel", this::onCancel));
        importWallet = new Button("Import Wallet", this::createWallet).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false));
        importWallet.setEnabled(false);
        buttonPanel.addComponent(importWallet);

        mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
        mainPanel.addComponent(new EmptySpace(TerminalSize.ZERO));

        buttonPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,false,false)).addTo(mainPanel);
        mainPanel.addComponent(new EmptySpace(TerminalSize.ZERO));

        setComponent(mainPanel);

        descriptor.setTextChangeListener((newText, changedByUserInteraction) -> {
            String line = newText.replaceAll("\\s+", "");
            try {
                Address parsed = Address.fromString(line);
                parsed.requireSendable();
                importWallet.setEnabled(true);
            } catch(Exception e1) {
                importWallet.setEnabled(false);
            }

            if(changedByUserInteraction) {
                List<String> lines = splitString(newText, descriptorWidth);
                String splitText = lines.stream().reduce((s1, s2) -> s1 + "\n" + s2).get();
                if(!newText.equals(splitText)) {
                    descriptor.setText(splitText);

                    TerminalPosition pos = descriptor.getCaretPosition();
                    if(pos.getRow() == lines.size() - 2 && pos.getColumn() == lines.get(lines.size() - 2).length()) {
                        descriptor.setCaretPosition(lines.size() - 1, lines.get(lines.size() - 1).length());
                    }
                }
            }
        });
    }

    @Override
    protected List<Wallet> getWallets() throws ImportException {
        String text = descriptor.getText().replaceAll("\\s+", "");
        try {
            Address parsed = Address.fromString(text);
            parsed.requireSendable();
            Wallet wallet = new Wallet(walletName);
            wallet.setPolicyType(PolicyType.SINGLE_HD);
            wallet.setScriptType(ScriptType.MLDSA87_SINGLE);
            Keystore keystore = new Keystore();
            keystore.setSource(KeystoreSource.SW_WATCH);
            keystore.setWalletModel(WalletModel.SPARROW);
            wallet.getKeystores().add(keystore);
            wallet.setDefaultPolicy(Policy.getPolicy(wallet.getPolicyType(), wallet.getScriptType(), wallet.getKeystores(), 1));
            WalletNode receive0 = new WalletNode(wallet, KeyPurpose.RECEIVE, 0);
            receive0.setAddress(parsed);
            wallet.getNode(KeyPurpose.RECEIVE).getChildren().add(receive0);
            return List.of(wallet);
        } catch(Exception e) {
            throw new ImportException("Watch-only needs an ML-DSA-44 address, a Sparrow wallet file, or seed words.", e);
        }
    }

    private List<String> splitString(String stringToSplit, int maxLength) {
        String text = stringToSplit.replaceAll("\\s+", "");
        if(stringToSplit.endsWith("\n")) {
            text += "\n";
        }

        List<String> lines = new ArrayList<>();
        while(text.length() >= maxLength) {
            int breakAt = maxLength - 1;
            lines.add(text.substring(0, breakAt));
            text = text.substring(breakAt);
        }

        if(text.equals("\n")) {
            text = "";
        }

        lines.add(text);
        return lines;
    }
}
