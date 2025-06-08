package com.esa.moviestar.settings;

import com.esa.moviestar.Main;
import com.esa.moviestar.model.Account;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Objects;

public class DeletePopUp extends StackPane {

    private Button deleteButton;
    private Button cancelButton;
    private PasswordField passwordField;
    private VBox passwordVBox;
    private TextField passwordTextField;
    private Button togglePasswordButton;
    private boolean isPasswordVisible = false;

    public DeletePopUp(boolean isAccount, Account account) {
        page(isAccount, account);
        passwordProperty();
        setupPasswordToggle();

    }

    private void page(boolean isAccount, Account account) {
        StackPane mainPane = new StackPane();

        // StackPane
        mainPane.setMaxHeight(340.0);
        mainPane.setMaxWidth(550.0);
        mainPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/esa/moviestar/styles/general.css")).toExternalForm());
        mainPane.getStyleClass().addAll("surface-dim-opaque", "very-large-item");
        mainPane.setPadding(new Insets(25.0));
        StackPane.setAlignment(mainPane, Pos.CENTER);

        // VBox
        VBox mainVBox = new VBox();
        mainVBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(mainVBox, Pos.CENTER);
        mainVBox.setSpacing(25.0);

        // Label title
        Label titleLabel = new Label(isAccount ? "Delete Account" : "Delete User");
        titleLabel.setPrefHeight(35.0);
        titleLabel.setPrefWidth(405.0);
        titleLabel.getStyleClass().addAll("large-text", "bold-text", "on-primary");

        // Text
        Text descriptionText = new Text(isAccount ?
                "Are you sure you want to delete your account? By proceeding, you will be logged out and will no longer be able to access it."
                :
                "Are you sure you want to delete your user profile? Deleting your profile is an irreversible action and you will lose all data associated with it.");
        descriptionText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        descriptionText.setStrokeWidth(0.0);
        descriptionText.setWrappingWidth(400.0);
        descriptionText.getStyleClass().addAll("medium-text", "on-primary");

        // VBox for password
        passwordVBox = new VBox();
        passwordVBox.setSpacing(5.0);

        // Label password
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().addAll("bold-text", "on-primary", "medium-text");
        VBox.setMargin(passwordLabel, new Insets(0, 0, 0, 40.0));

        //password
        passwordField = new PasswordField();
        passwordField.getStyleClass().addAll("on-primary", "small-item", "medium-text", "surface-dim-border", "text-area");

        passwordTextField = new TextField();
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        passwordTextField.getStyleClass().addAll("on-primary", "small-item", "medium-text", "surface-dim-border", "text-area");

        togglePasswordButton = new Button();
        togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        togglePasswordButton.getStyleClass().add("back-button");

        StackPane passwordStack = new StackPane(passwordField, passwordTextField, togglePasswordButton);
        passwordStack.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordButton, new Insets(0, 10, 0, 0));
        VBox.setMargin(passwordStack, new Insets(0, 40.0, 0, 40.0));

        VBox.setMargin(passwordField, new Insets(0, 40.0, 0, 40.0));

        passwordVBox.getChildren().addAll(passwordLabel, passwordStack);

        // Buttons
        HBox buttonHBox = new HBox();
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.setPrefHeight(100.0);
        buttonHBox.setPrefWidth(200.0);
        buttonHBox.setSpacing(35.0);
        VBox.setMargin(buttonHBox, new Insets(0, 40.0, 0, 0));

        // Pulsante Annulla
        cancelButton = new Button("Cancel");
        cancelButton.setMnemonicParsing(false);
        cancelButton.getStyleClass().addAll("medium-item", "back-button");

        // Pulsante Elimina account
        deleteButton = new Button(isAccount ? "Delete account" : "Delete user");
        deleteButton.setMnemonicParsing(false);
        deleteButton.setPrefHeight(35.0);
        deleteButton.setPrefWidth(110.0);
        deleteButton.getStyleClass().addAll("small-item", "on-primary", "surface-danger");

        // Delete button is initialized as disable
        deleteButton.setDisable(true);

        buttonHBox.getChildren().addAll(cancelButton, deleteButton);

        // Add all to main
        mainVBox.getChildren().addAll(titleLabel, descriptionText, passwordVBox, buttonHBox);
        mainPane.getChildren().add(mainVBox);

        this.getChildren().add(mainPane);

        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
    }

    private void passwordProperty() {
        // Listener per abilitare/disabilitare il pulsante in base al contenuto del TextField
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
    }

    private void setupPasswordToggle() {
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordTextField.isFocused()) {
                passwordTextField.setText(newText);
            }
        });

        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newText);
            }
        });

        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        passwordField.setVisible(!isPasswordVisible);
        passwordField.setManaged(!isPasswordVisible);
        passwordTextField.setVisible(isPasswordVisible);
        passwordTextField.setManaged(isPasswordVisible);

        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
            togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});;
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }
    }


    // Getter to access at the contents
    public Button getDeleteButton() {
        return deleteButton;
    }

    public VBox getPasswordBox() {
        return passwordVBox;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public TextField getPasswordField() {
        return passwordField;
    }
}