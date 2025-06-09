package com.esa.moviestar.login;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.libraries.CredentialCryptManager;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ResetController{
    //Svg container
    @FXML
    private StackPane backToLoginButton;
    //Button to reset the password
    @FXML
    private Button resetButton;
    @FXML
    public Label titleText;
    //Warning texts and countdown label
    @FXML
    public Label warningText;
    @FXML
    public Label warningText2;
    @FXML
    public Label countdownLabel;
    //Password and password toggle fields
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private Button toggleNewPasswordButton;
    @FXML
    private Button toggleConfirmPasswordButton;


    @FXML
    private VBox mainContainer;

    @FXML
    private Label statusMessage;

    @FXML
    private StackPane parentContainer;

    @FXML
    private StackPane newPasswordContainer;

    @FXML
    private StackPane confirmPasswordContainer;
    //Reset email setups
    private String userEmail;
    private String expectedVerificationCode;
    private Timeline countdownTimeline;
    //Max timer before the code expires
    private int timeSeconds = 120;
    private boolean codeExpired = false;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    //Reference quantities for the responsive layout
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_CONTAINER_WIDTH = 500.0;
    private final double REFERENCE_CONTAINER_HEIGHT = 559.0;
    //Currently the compact mode is unused due to general settings of the application
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;

    public void initialize() {
        setupBasicLayout();
        setupPasswordToggle();

        if (resetButton != null && newPasswordField != null &&
                confirmPasswordField != null && codeField != null && backToLoginButton != null) {
            Node[] formElements = {toggleConfirmPasswordButton, toggleNewPasswordButton, newPasswordField, confirmPasswordField, backToLoginButton, codeField, newPasswordContainer, confirmPasswordContainer, resetButton};
            AnimationUtils.animateSimultaneously(formElements, 1, 0.3);
        }

        setupResponsiveLayout();
    }

    private void setupBasicLayout(){
        statusMessage.setText("");
        countdownLabel.setText("");
        codeField.setPromptText("Verification code");
        newPasswordField.setPromptText("New password");
        newPasswordTextField.setPromptText("New password");
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordTextField.setPromptText("Confirm new password");
        toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        toggleConfirmPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        if(resetButton != null)
            resetButton.setOnAction(event -> validatePasswordReset());

        if(backToLoginButton != null)
            backToLoginButton.setOnMouseClicked(event -> navigateToLogin());
    }
    // Password toggle setup similar to Access class
    private void setupPasswordToggle(){
        // Setup new password toggle
        if(newPasswordField != null && newPasswordTextField != null && toggleNewPasswordButton != null){
            newPasswordField.textProperty().addListener((obs, oldText, newText) ->{
                if (!newPasswordTextField.isFocused())
                    newPasswordTextField.setText(newText);
            });
            newPasswordTextField.textProperty().addListener((obs, oldText, newText) ->{
                if(!newPasswordField.isFocused())
                    newPasswordField.setText(newText);
            });

            toggleNewPasswordButton.setOnAction(event -> toggleNewPasswordVisibility());
            StackPane.setAlignment(toggleNewPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleNewPasswordButton, new Insets(0, 10, 0, 0));
        }

        // Setup confirm password toggle
        if(confirmPasswordField != null && confirmPasswordTextField != null && toggleConfirmPasswordButton != null){
            confirmPasswordField.textProperty().addListener((obs, oldText, newText) ->{
                if(!confirmPasswordTextField.isFocused())
                    confirmPasswordTextField.setText(newText);
            });

            confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
                if(!confirmPasswordField.isFocused())
                    confirmPasswordField.setText(newText);
            });
            toggleConfirmPasswordButton.setOnAction(event -> toggleConfirmPasswordVisibility());
            StackPane.setAlignment(toggleConfirmPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleConfirmPasswordButton, new Insets(0, 10, 0, 0));
        }

    }


    private void toggleNewPasswordVisibility(){
        isNewPasswordVisible = !isNewPasswordVisible;

        if(isNewPasswordVisible){
            newPasswordTextField.setText(newPasswordField.getText());
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            newPasswordTextField.setVisible(true);
            newPasswordTextField.setManaged(true);
            newPasswordTextField.requestFocus();
            newPasswordTextField.positionCaret(newPasswordTextField.getText().length());
            toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});
        }
        else{
            newPasswordField.setText(newPasswordTextField.getText());
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());
            toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }
    }

    private void toggleConfirmPasswordVisibility(){
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if(isConfirmPasswordVisible){
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
            toggleConfirmPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});
        }
        else{
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
            toggleConfirmPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }
    }

    private String getCurrentNewPassword(){
        return isNewPasswordVisible ? newPasswordTextField.getText() : newPasswordField.getText();}

    private String getCurrentConfirmPassword(){
        return isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();}

    private void setupResponsiveLayout(){
        if(parentContainer != null){
            parentContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.widthProperty().addListener((observable, oldValue, newValue) ->
                            adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                    newScene.heightProperty().addListener((observable, oldValue, newValue) ->
                            adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                    adjustLayout(newScene.getWidth(), newScene.getHeight()); // Initial adjustment
                }
            });
        }
    }

    private void setupVBoxLayout(double containerWidth, double containerHeight){
        mainContainer.setPrefWidth(containerWidth);
        mainContainer.setPrefHeight(containerHeight);
        mainContainer.setMaxWidth(containerWidth);
        mainContainer.setMaxHeight(containerHeight);
    }

    private void setupTextFontSize(double baseFontSize){
        if (statusMessage != null)
            statusMessage.setStyle("-fx-font-size: " + (baseFontSize) + "px;");

        if(warningText != null)
            warningText.setStyle("-fx-font-size: " + (baseFontSize) + "px;");

        if(warningText2 != null)
            warningText2.setStyle("-fx-font-size: " + (baseFontSize) + "px;");
        if(titleText != null)
            titleText.setStyle("-fx-font-size: " + (baseFontSize * 1.5) + "px;");
    }
    private void setupFieldLayout(double fieldWidth){
        codeField.setPrefWidth(fieldWidth);
        codeField.setMaxWidth(fieldWidth);

        newPasswordContainer.setPrefWidth(fieldWidth);
        newPasswordContainer.setMaxWidth(fieldWidth);

        newPasswordField.setPrefWidth(fieldWidth);
        newPasswordField.setMaxWidth(fieldWidth);

        newPasswordTextField.setPrefWidth(fieldWidth);
        newPasswordTextField.setMaxWidth(fieldWidth);

        confirmPasswordContainer.setPrefWidth(fieldWidth);
        confirmPasswordContainer.setMaxWidth(fieldWidth);

        confirmPasswordField.setPrefWidth(fieldWidth);
        confirmPasswordField.setMaxWidth(fieldWidth);

        confirmPasswordTextField.setPrefWidth(fieldWidth);
        confirmPasswordTextField.setMaxWidth(fieldWidth);

    }
    //We simply don't need to set up horizontal margins for the vbox
    private void setupVBoxMargin(double verticalMargin){
        VBox.setMargin(codeField, new Insets((verticalMargin + 25), 0, verticalMargin, 0));
        VBox.setMargin(newPasswordContainer, new Insets(verticalMargin, 0, 0, 0));
        VBox.setMargin(confirmPasswordContainer, new Insets(0, 0, 0, 0));
        VBox.setMargin(resetButton, new Insets((verticalMargin + 10), 0, 0, 0));
        VBox.setMargin(backToLoginButton, new Insets((verticalMargin), 0, verticalMargin - 30, verticalMargin - 200));
    }

    private void adjustLayout(double width, double height){
        //Scale factor to keep the same aspect ratio
        double scale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        scale = 1 - (1 - scale) * 0.5;

        //Handle main container layout
        if(mainContainer != null){
            boolean showContainer = width > MIN_VBOX_VISIBILITY_THRESHOLD;
            mainContainer.setVisible(showContainer);
            mainContainer.setManaged(showContainer);

            if(showContainer){
                //Compact mode is currently unable
                boolean compactMode = width < COMPACT_MODE_THRESHOLD;

                double containerWidth = compactMode ? 280 : REFERENCE_CONTAINER_WIDTH * scale;
                double containerHeight = compactMode ? 300 : REFERENCE_CONTAINER_HEIGHT * scale;
                setupVBoxLayout(containerWidth, containerHeight);
                double padding = Math.max(10, 20 * scale);
                double spacing = Math.max(5, 10 * scale);
                mainContainer.setPadding(new Insets(padding));
                mainContainer.setSpacing(spacing);

                StackPane.setAlignment(mainContainer, compactMode ? Pos.CENTER : Pos.CENTER);
                StackPane.setMargin(mainContainer, compactMode ? new Insets(0) : new Insets(0));
                double baseFontSize = 16 * scale;
                double buttonScale = Math.max(scale, 0.7);
                setupTextFontSize(baseFontSize);
                if (resetButton != null) {
                    // Smaller font size for button
                    resetButton.setStyle("-fx-font-size: " + (Math.min(18 * buttonScale, 18)+5) + "px;");
                }

                if (backToLoginButton != null) {
                    backToLoginButton.setStyle("-fx-font-size: " + Math.min(14 * buttonScale, 14) + "px; -fx-alignment: CENTER_LEFT;");
                    backToLoginButton.setAlignment(Pos.CENTER_LEFT);
                }

                double fieldWidth = (Math.min(containerWidth - padding * 2, containerWidth * 0.9) - 15);
                setupFieldLayout(fieldWidth);


                // Dynamic margins
                double verticalMargin = 10 * scale;
                setupVBoxMargin(verticalMargin);

            }
        }
    }

    public void setUserEmail(String email){
        this.userEmail = email;}

    private void enableFields(boolean enable){
        if(resetButton != null) resetButton.setDisable(!enable);
        if(codeField != null) codeField.setDisable(!enable);
        if(newPasswordField != null) newPasswordField.setDisable(!enable);
        if(newPasswordTextField != null) newPasswordTextField.setDisable(!enable);
        if(confirmPasswordField != null) confirmPasswordField.setDisable(!enable);
        if(confirmPasswordTextField != null) confirmPasswordTextField.setDisable(!enable);

        if(!enable){
            if(codeField != null) codeField.setText("");
            if(newPasswordField != null) newPasswordField.setText("");
            if(newPasswordTextField != null) newPasswordTextField.setText("");
            if(confirmPasswordField != null) confirmPasswordField.setText("");
            if(confirmPasswordTextField != null) confirmPasswordTextField.setText("");
        }
    }
    public void setVerificationCodeStartTimer(String code){
        this.expectedVerificationCode = code;
        startCountdownTimer();
    }

    private void startCountdownTimer(){
        timeSeconds = 120;
        codeExpired = false;
        enableFields(true);

        if (countdownTimeline != null)
            countdownTimeline.stop();

        countdownTimeline = new Timeline();
        countdownTimeline.setCycleCount(timeSeconds + 1);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event ->{
            if(timeSeconds > 0){
                final int currentSeconds = timeSeconds;
                Platform.runLater(() -> {
                    if (countdownLabel != null) {
                        countdownLabel.setText("Code expires in: " + currentSeconds + " seconds");
                        countdownLabel.setStyle("-fx-text-fill: #F0ECFD; -fx-font-weight: normal; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");
                    }
                });
                timeSeconds--;
            }
            else{
                Platform.runLater(() ->{
                    if(countdownLabel != null) {
                        countdownLabel.setText("Code expired. Please return to Access and try again.");
                        countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");
                    }
                    enableFields(false);
                    codeExpired = true;
                });
                countdownTimeline.stop();
            }
        });
        countdownTimeline.getKeyFrames().add(keyFrame);
        countdownTimeline.playFromStart();
    }

    public void setupResetButton(){
        if (resetButton != null)
            resetButton.setOnAction(event -> validatePasswordReset());
    }

    private void validatePasswordReset(){
        if(codeExpired){
            updateStatus("Verification code has expired. Please request a new one.");
            AnimationUtils.shake(statusMessage);
            if(countdownLabel != null){
                countdownLabel.setText("Code expired. Please return to Access and try again.");
                countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");
            }
            enableFields(false);
            return;
        }
        String inputCode = codeField.getText();
        String newPassword = getCurrentNewPassword();
        String confirmPassword = getCurrentConfirmPassword();

        if(inputCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
            updateStatus("Fields cannot be empty");
            AnimationUtils.shake(statusMessage);
            return;
        }
        if(!inputCode.equals(expectedVerificationCode)){
            updateStatus("Incorrect verification code");
            AnimationUtils.shake(statusMessage);
            return;
        }
        if(!newPassword.equals(confirmPassword)){
            updateStatus("Passwords do not match");
            AnimationUtils.shake(statusMessage);
            return;
        }

        Register tempRegister = new Register();
        if(!Pattern.matches(tempRegister.get_regex(), newPassword)){
            updateStatus("Password does not meet security requirements");
            AnimationUtils.shake(statusMessage);
            return;
        }
        try{
            String hashedPassword = CredentialCryptManager.hashPassword(newPassword);
            swapPassword(userEmail, hashedPassword);
            updateStatus("Password changed successfully");
            AnimationUtils.pulse(resetButton);
            if(countdownTimeline != null)
                countdownTimeline.stop();
            if(countdownLabel != null){
                countdownLabel.setText("Password reset successful!");
                countdownLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }
            enableFields(false);
            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToLogin());

            pause.play();

        }
        catch(SQLException e){
            updateStatus("Error during password reset: " + e.getMessage());
        }
    }

    private void swapPassword(String email, String hashedPassword) throws SQLException{
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, hashedPassword);
    }

    private void updateStatus(String message){
        if (statusMessage != null)
            statusMessage.setText(message);
        else
            System.out.println("Status message label not found: " + message);
    }

    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    private void navigateToLogin(){
        if(countdownTimeline != null)
            countdownTimeline.stop();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"),resourceBundle);
            Parent loginContent = loader.load();
            Scene currentScene = parentContainer.getScene();
            Scene newScene = new Scene(loginContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);
        }
        catch(IOException e){
            updateStatus("An error occurred while navigating to login: " + e.getMessage());
        }
    }
}