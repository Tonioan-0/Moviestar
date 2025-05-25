package com.esa.moviestar.settings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Objects;

public class DeletePopUp extends StackPane {

    private Button deleteButton;
    private Button cancelButton;
    private TextField passwordField;

    public DeletePopUp(boolean isAccount) {
        StackPane mainPane = new StackPane();

        // StackPane per l'UI principale
        mainPane.setMaxHeight(340.0);
        mainPane.setMaxWidth(551.0);
        mainPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/esa/moviestar/styles/general.css")).toExternalForm());
        mainPane.getStyleClass().addAll("surface-dim-opaque", "very-large-item");
        mainPane.setPadding(new Insets(24.0));
        StackPane.setAlignment(mainPane,Pos.CENTER);

        // VBox principale
        VBox mainVBox = new VBox();
        mainVBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(mainVBox,Pos.CENTER);
        mainVBox.setSpacing(24.0);

        // Label titolo
        Label titleLabel = new Label(isAccount?"Elimina account":"Elimina utente");
        titleLabel.setPrefHeight(35.0);
        titleLabel.setPrefWidth(406.0);
        titleLabel.getStyleClass().addAll("large-text", "bold-text", "on-primary");

        // Testo descrittivo
        Text descriptionText = new Text(isAccount?"Sei sicuro di voler eliminare il tuo account ? Procedendo con l'eliminazione verrai disconesso e non potrai accedere":"Sei sicuro di voler eliminare il tuo profilo utente ? L'eliminazione del tuo profilo è un'azione irreversibile e perderai tutti i dati associato ad esso . ");
        descriptionText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        descriptionText.setStrokeWidth(0.0);
        descriptionText.setWrappingWidth(400.0);
        descriptionText.getStyleClass().addAll("medium-text", "on-primary");

        // VBox per password
        VBox passwordVBox = new VBox();
        passwordVBox.setSpacing(5.0);

        // Label password
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().addAll("bold-text", "on-primary", "medium-text");
        VBox.setMargin(passwordLabel, new Insets(0, 0, 0, 40.0));

        // TextField password
        passwordField = new TextField();
        passwordField.getStyleClass().addAll( "on-primary", "small-item","medium-text","surface-dim-border","text-area");
        VBox.setMargin(passwordField, new Insets(0, 40.0, 0, 40.0));

        passwordVBox.getChildren().addAll(passwordLabel, passwordField);

        // HBox per i pulsanti
        HBox buttonHBox = new HBox();
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.setPrefHeight(100.0);
        buttonHBox.setPrefWidth(200.0);
        buttonHBox.setSpacing(36.0);
        VBox.setMargin(buttonHBox, new Insets(0, 40.0, 0, 0));

        // Pulsante Annulla
        cancelButton = new Button("Annulla");
        cancelButton.setMnemonicParsing(false);
        cancelButton.getStyleClass().addAll("medium-item", "back-button");

        // Pulsante Elimina account
        deleteButton = new Button(isAccount?"Elimina account":"Elimina utente");
        deleteButton.setMnemonicParsing(false);
        deleteButton.setPrefHeight(35.0);
        deleteButton.setPrefWidth(110.0);
        deleteButton.getStyleClass().addAll("small-item", "on-primary", "surface-danger");

        // Il pulsante elimina è inizialmente disabilitato
        deleteButton.setDisable(true);

        // Listener per abilitare/disabilitare il pulsante in base al contenuto del TextField
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });

        buttonHBox.getChildren().addAll(cancelButton, deleteButton);

        // Assemblaggio finale
        mainVBox.getChildren().addAll(titleLabel, descriptionText, passwordVBox, buttonHBox);
        mainPane.getChildren().add(mainVBox);

        this.getChildren().add(mainPane);

        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
    }


    // Getter per accedere ai componenti dall'esterno
    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public TextField getPasswordField() {
        return passwordField;
    }
}