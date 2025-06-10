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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
/*This class job is to reset the password by managing the reset-view-scene and the email service
by sending a verification code to the user's email and changing password if it's correct */
public class ResetController{


    @FXML private StackPane parentContainer;
    @FXML private StackPane newPasswordContainer;
    @FXML private StackPane confirmPasswordContainer;

    //Warning texts and countdown label
    @FXML public Label warningText;
    @FXML public Label warningText2;
    @FXML public Label countdownLabel;
    @FXML public Label titleText;
    @FXML private Label statusMessage;
    public Label warning1;
    public Label warning2;
    public Label warningSpecial;
    public Label warningSpecial2;

    //Password and password toggle fields
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private TextField newPasswordTextField;
    @FXML private TextField codeField;
    @FXML private Button toggleNewPasswordButton;
    @FXML private Button toggleCheckPasswordButton;
    @FXML private Button resetButton;//Button to reset the password

    @FXML private StackPane backToLoginButton; //Svg container
    @FXML private VBox mainContainer;


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
            Node[] formElements = getElements();
            AnimationUtils.animateSimultaneously(formElements);
        }

        setupResponsiveLayout();
    }

    private Node [] getElements() {
        Node[] formElements = {toggleCheckPasswordButton, toggleNewPasswordButton, newPasswordField, confirmPasswordField, backToLoginButton, codeField, newPasswordContainer, confirmPasswordContainer, resetButton};
        return formElements;
    }

    private void setupBasicLayout(){
        statusMessage.setText("");
        countdownLabel.setText("");
        warning1.setText("   Password must contain at least:");
        warning2.setText("    · 8 characters");
        warningSpecial.setText("    · 1 number");
        warningSpecial2.setText("    · 1 special character (! @ # $ % ^ & *)");
        codeField.setPromptText("Verification code");
        newPasswordField.setPromptText("New password");
        newPasswordTextField.setPromptText("New password");
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordTextField.setPromptText("Confirm new password");
        toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        toggleCheckPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        if(resetButton != null)
            resetButton.setOnAction(event -> validatePasswordReset());

        if(backToLoginButton != null)
            backToLoginButton.setOnMouseClicked(event -> navigateToLogin());
    }
    // Password toggle setup similar to Access class
    private void setupPasswordToggle(){
        // Setup new password toggle

        newPasswordField.textProperty().addListener((obs, oldText, newText) ->{if (!newPasswordTextField.isFocused()) newPasswordTextField.setText(newText);});
        newPasswordTextField.textProperty().addListener((obs, oldText, newText) ->{

            if(!newPasswordField.isFocused()) newPasswordField.setText(newText);});

        toggleNewPasswordButton.setOnAction(event -> toggleNewPasswordVisibility());
        StackPane.setAlignment(toggleNewPasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleNewPasswordButton, new Insets(0, 10, 0, 0));

        // Setup confirm password toggle
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) ->{

            if(!confirmPasswordTextField.isFocused()) confirmPasswordTextField.setText(newText);});

        confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {

            if(!confirmPasswordField.isFocused()) confirmPasswordField.setText(newText);});

        toggleCheckPasswordButton.setOnAction(event -> toggleConfirmPasswordVisibility());
        StackPane.setAlignment(toggleCheckPasswordButton, Pos.CENTER_RIGHT);

        StackPane.setMargin(toggleCheckPasswordButton, new Insets(0, 10, 0, 0));
    }


    private void toggleNewPasswordVisibility(){
        isNewPasswordVisible = !isNewPasswordVisible;

        if(!isNewPasswordVisible){

            newPasswordField.setText(newPasswordTextField.getText());
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);

            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());

            toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }

        else{

            newPasswordTextField.setText(newPasswordField.getText());
            newPasswordField.setVisible(false);

            newPasswordField.setManaged(false);

            newPasswordTextField.setVisible(true);

            newPasswordTextField.setManaged(true);
            newPasswordTextField.requestFocus();

            newPasswordTextField.positionCaret(newPasswordTextField.getText().length());
            toggleNewPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});


        }
    }

    private void showPasswordField(){
        confirmPasswordTextField.setText(confirmPasswordField.getText());
        confirmPasswordField.setVisible(false);

        confirmPasswordTextField.setVisible(true);

        confirmPasswordField.setManaged(false);

        confirmPasswordTextField.setManaged(true);

        confirmPasswordTextField.requestFocus();
        confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
    }
    private void hidePasswordField(){
        confirmPasswordField.setText(confirmPasswordTextField.getText());

        confirmPasswordTextField.setVisible(false);
        confirmPasswordField.setVisible(true);

        confirmPasswordTextField.setManaged(false);


        confirmPasswordField.setManaged(true);
        confirmPasswordField.requestFocus();
        confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
    }
    private String getCurrentConfirmPassword(){
        return isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();}
    
    private void toggleConfirmPasswordVisibility(){
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if(isConfirmPasswordVisible){

            showPasswordField();
            toggleCheckPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});
        }
        else{
            hidePasswordField();
            toggleCheckPasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }
    }

    private String getCurrentNewPassword(){
        return isNewPasswordVisible ? newPasswordTextField.getText() : newPasswordField.getText();}


    private void setupResponsiveLayout(){

        parentContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newScene.getWidth(), newValue.doubleValue()));

                adjustLayout(newScene.getWidth(), newScene.getHeight());
            }
        });
    }

    private void setupVBoxLayout(double containerWidth, double containerHeight){
        mainContainer.setPrefWidth(containerWidth);
        mainContainer.setPrefHeight(containerHeight);

        mainContainer.setMaxWidth(containerWidth);
        mainContainer.setMaxHeight(containerHeight);
    }

    private void setupTextFontSize(double baseFontSize){
        statusMessage.setStyle("-fx-font-size: " + (baseFontSize) + "px;");
        warningText.setStyle("-fx-font-size: " + (baseFontSize) + "px;");
        warning1.setStyle("-fx-font-size: " + (baseFontSize + 2) + "px;");
        warning2.setStyle("-fx-font-size: " + (baseFontSize + 2) + "px;");
        warningSpecial.setStyle("-fx-font-size: " + (baseFontSize + 2) + "px;");
        warningSpecial2.setStyle("-fx-font-size: " + (baseFontSize + 2) + "px;");
        warningText2.setStyle("-fx-font-size: " + (baseFontSize) + "px;");
        titleText.setStyle("-fx-font-size: " + (baseFontSize * 1.5) + "px;");
    }
    //Function to set the fields width
    private void setupFieldLayout(double fieldWidth){

        codeField.setPrefWidth(fieldWidth);
        newPasswordField.setPrefWidth(fieldWidth);
        newPasswordTextField.setMaxWidth(fieldWidth);

        newPasswordTextField.setPrefWidth(fieldWidth);

        confirmPasswordContainer.setPrefWidth(fieldWidth);
        newPasswordContainer.setPrefWidth(fieldWidth);
        newPasswordContainer.setMaxWidth(fieldWidth);

        codeField.setMaxWidth(fieldWidth);
        newPasswordField.setMaxWidth(fieldWidth);

        confirmPasswordContainer.setMaxWidth(fieldWidth);
        confirmPasswordField.setPrefWidth(fieldWidth);
        confirmPasswordField.setMaxWidth(fieldWidth);

        confirmPasswordTextField.setPrefWidth(fieldWidth);
        confirmPasswordTextField.setMaxWidth(fieldWidth);
    }
    //We simply don't need to set up horizontal margins for the vbox
    private void setupVBoxMargin(double verticalMargin){
        double baseMargin = verticalMargin;

        if(codeField != null)VBox.setMargin(codeField, new Insets((verticalMargin + 25), 0, baseMargin, 0));
        if(newPasswordContainer != null)VBox.setMargin(newPasswordContainer, new Insets(baseMargin, 0, 0, 0));

        if(confirmPasswordContainer != null)VBox.setMargin(confirmPasswordContainer, new Insets(0, 0, 0, 0));
        if(resetButton != null)VBox.setMargin(resetButton, new Insets((verticalMargin + 10), 0, 0, 0));
        if(backToLoginButton != null)VBox.setMargin(backToLoginButton, new Insets((baseMargin), 0, verticalMargin - 30, verticalMargin - 200));
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
                double containerHeight = (compactMode ? 300 : REFERENCE_CONTAINER_HEIGHT * scale)+50;
                setupVBoxLayout(containerWidth, containerHeight);
                double padding = Math.max(10, 20 * scale);

                double spacing = Math.max(5, 10 * scale);

                mainContainer.setPadding(new Insets(padding));
                mainContainer.setSpacing(spacing);

                StackPane.setAlignment(mainContainer, Pos.CENTER);
                StackPane.setMargin(mainContainer,new Insets(0));
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
    //Function to enable the fields 
    
    private void enableElements(boolean enable){
        if(!enable){
            codeField.setText("");
            newPasswordField.setText("");
            newPasswordTextField.setText("");

            confirmPasswordField.setText("");
            confirmPasswordTextField.setText("");
        }
        resetButton.setDisable(!enable);
        codeField.setDisable(!enable);
        newPasswordField.setDisable(!enable);

        newPasswordTextField.setDisable(!enable);

        confirmPasswordField.setDisable(!enable);
        confirmPasswordTextField.setDisable(!enable);


    }
    public void setVerificationCodeStartTimer(String code){
        this.expectedVerificationCode = code;
        startCountdownTimer();
    }
    /*Starts the countdown timer, when it ends no input can be accepted except for going back to the access,
    with the statu message updated*/
    private void startCountdownTimer(){
        timeSeconds = 120;
        codeExpired = false;
        enableElements(true);

        if (countdownTimeline != null)
            countdownTimeline.stop();

        countdownTimeline = new Timeline();
        countdownTimeline.setCycleCount(timeSeconds + 1);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event ->{
            
            if(timeSeconds > 0){
                
                Platform.runLater(() -> {
                    if (countdownLabel != null) {
                        int currentSeconds = timeSeconds;
                        
                        countdownLabel.setText("Code expires in: " + currentSeconds + " seconds");
                        countdownLabel.setStyle("-fx-text-fill: #F0ECFD; -fx-font-weight: normal; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");
                    }
            
                });
                timeSeconds--;
            }
            else{
                Platform.runLater(() ->{
                    countdownLabel.setText("Code expired. Please return to Access and try again.");

                    countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");
                    enableElements(false);

                    codeExpired = true;
                });
                countdownTimeline.stop();
            }
        });
        countdownTimeline.getKeyFrames().add(keyFrame);

        countdownTimeline.playFromStart();
    }

    public void setupResetButton(){
        if (resetButton != null)resetButton.setOnAction(event -> validatePasswordReset());}

    private void validatePasswordReset(){
        if(codeExpired){
            updateStatus("Verification code has expired. Please request a new one.");

            AnimationUtils.shake(statusMessage);
            countdownLabel.setText("Code expired. Please return to Access and try again.");
            countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: " + (16 * Math.min(1, parentContainer.getWidth()/REFERENCE_WIDTH) * 0.9) + "px;");

            enableElements(false);
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
        try
        {

            String hashedPassword = CredentialCryptManager.hashPassword(newPassword);
            swapPassword(userEmail, hashedPassword);
            AnimationUtils.pulse(resetButton);
            countdownTimeline.stop();
            if(countdownLabel != null){
                countdownLabel.setText("Password reset successful!");
                countdownLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }
            enableElements(false);
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
    //Function to communicate the user the current status of the operations
    private void updateStatus(String message){
        statusMessage.setText(message);}

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