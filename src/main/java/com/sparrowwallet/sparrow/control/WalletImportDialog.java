package com.sparrowwallet.sparrow.control;

import com.google.common.eventbus.Subscribe;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.sparrow.AppServices;
import com.sparrowwallet.sparrow.EventManager;
import com.sparrowwallet.sparrow.event.WalletImportEvent;
import com.sparrowwallet.sparrow.io.*;
import com.sparrowwallet.sparrow.wallet.WalletForm;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WalletImportDialog extends Dialog<List<Wallet>> {
    private List<Wallet> wallets;
    private final Accordion importAccordion;

    public WalletImportDialog(List<WalletForm> selectedWalletForms) {
        EventManager.get().register(this);
        setOnCloseRequest(event -> {
            EventManager.get().unregister(this);
        });

        final DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(AppServices.class.getResource("general.css").toExternalForm());
        AppServices.setStageIcon(dialogPane.getScene().getWindow());

        StackPane stackPane = new StackPane();
        dialogPane.setContent(stackPane);

        AnchorPane anchorPane = new AnchorPane();
        stackPane.getChildren().add(anchorPane);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(520);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        anchorPane.getChildren().add(scrollPane);
        scrollPane.setFitToWidth(true);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        importAccordion = new Accordion();
        List<WalletImport> walletImporters = new ArrayList<>(List.of(new Sparrow()));
        if(!selectedWalletForms.isEmpty()) {
            walletImporters.add(new WalletLabels(selectedWalletForms));
        }
        for(WalletImport importer : walletImporters) {
            if(importer.getWalletModel().isProductImport() && (!importer.isDeprecated() || Config.get().isShowDeprecatedImportExport())) {
                FileWalletImportPane importPane = new FileWalletImportPane(importer);
                importAccordion.getPanes().add(importPane);
            }
        }

        importAccordion.getPanes().sort(Comparator.comparing(o -> ((TitledDescriptionPane) o).getTitle()));

        MnemonicWalletKeystoreImportPane mnemonicImportPane = new MnemonicWalletKeystoreImportPane(new Bip39());
        importAccordion.getPanes().add(0, mnemonicImportPane);

        scrollPane.setContent(importAccordion);

        final ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(cancelButtonType);

        dialogPane.setPrefWidth(500);
        dialogPane.setPrefHeight(600);
        dialogPane.setMinHeight(dialogPane.getPrefHeight());
        AppServices.moveToActiveWindowScreen(this);

        setResultConverter(dialogButton -> dialogButton != cancelButtonType ? wallets : null);
    }

    @Subscribe
    public void walletImported(WalletImportEvent event) {
        wallets = event.getWallets();
        setResult(wallets);
    }
}
