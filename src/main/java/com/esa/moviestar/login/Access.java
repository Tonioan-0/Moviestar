package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.Main;
import com.esa.moviestar.libraries.EmailService;
import com.esa.moviestar.profile.ProfileView;
import com.esa.moviestar.model.Account;
import  com.esa.moviestar.libraries.CredentialCryptManager;
import jakarta.mail.MessagingException;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class Access {
    @FXML
    private Label welcomeText;
    @FXML
    private Label warningText;
    //Managing general main fxml elements
    @FXML
    private StackPane mainContainer;
    @FXML
    private VBox loginBox;
    /*Button section respectively, switching to register scene ,access logging button
    switching to reset password scene*/
    @FXML
    private Button register;
    @FXML
    private Button access;
    @FXML
    private Button resetPassword;
    //Managing general input access section
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private StackPane passwordContainer;
    @FXML
    private StackPane imageContainer;

    //Managing general email reset section
    private EmailService emailService;
    private boolean isPasswordVisible = false;

    private Account account;
    //Reference quantities for the responsive layout
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_LOGIN_WIDTH = 400.0;
    private final double REFERENCE_LOGIN_HEIGHT = 459.0;
    private final double REFERENCE_IMAGE_WIDTH = 700.0;
    private final double REFERENCE_IMAGE_HEIGHT = 194.0;
    //Currently the compact mode is unused due to general settings of the application
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double IMAGE_VISIBILITY_THRESHOLD = 600.0;

    public void initialize(){

        setupBasicGeneralObjects();
        setupPasswordToggle();
        setupBasicButtons();

        Node[] formElements = {togglePasswordButton,welcomeText, emailField, passwordField, access, register};
        AnimationUtils.animateSimultaneously(formElements, 1);

        setupResponsiveLayout();
        setupKeyboardNavigation();
    }

    private void setupBasicButtons(){
        register.setOnAction(event -> switchToRegistrationPage());
        access.setOnAction(event -> {
            try {
                loginUser();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        resetPassword.setOnAction(event -> {
            try {
                sendCredential();
            } catch (SQLException | IOException | MessagingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupBasicGeneralObjects(){
        emailService = new EmailService();
        emailField.setPromptText("Email");
        mainContainer.setMinWidth(1280);
        mainContainer.setMinHeight(720);
        passwordField.setPromptText("Password");
        passwordTextField.setPromptText("Password");
        warningText.setText("");
        emailField.setMinWidth(200);
        passwordField.setMinWidth(200);
        passwordTextField.setMinWidth(200);
        welcomeText.setText("WELCOME");
        access.setText("Access");
        register.setText("Don't have an account? Sign up");
        resetPassword.setText("Password forgotten? get it back");
        togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
    }
    //Setup password listeners and button toggle
    private void setupPasswordToggle(){
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

        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordButton, new Insets(0, 10, 0, 0));
    }

    private void togglePasswordVisibility(){
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
            togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.hidePassword"));getStyleClass().add("on-primary");}});
        }
        else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setGraphic(new SVGPath(){{setContent(Main.resourceBundle.getString("passwordField.showPassword"));getStyleClass().add("on-primary");}});
        }
    }

    private void setupKeyboardNavigation(){
        mainContainer.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER)
                access.fire();
        });
        emailField.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER) {
                if (isPasswordVisible)
                    passwordTextField.requestFocus();
                else
                    passwordField.requestFocus();
            }
        });

        passwordField.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER)
                access.fire();
        });
        passwordTextField.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER)
                access.fire();
        });
    }

    private void setupResponsiveLayout(){
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) ->{
            if (newScene != null){
                newScene.widthProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                adjustLayout(newScene.getWidth(), newScene.getHeight());
            }
        });
    }

    //Setup responsive starting layout quantities

    private void setupImageContainerLayout(double scale){
        imageContainer.setMinWidth(REFERENCE_IMAGE_WIDTH * scale);
        imageContainer.setMinHeight(REFERENCE_IMAGE_HEIGHT * scale);
        imageContainer.setPrefWidth(REFERENCE_IMAGE_WIDTH * scale);
        imageContainer.setPrefHeight(REFERENCE_IMAGE_HEIGHT * scale);
        imageContainer.setMaxWidth(REFERENCE_IMAGE_WIDTH * scale);
        imageContainer.setMaxHeight(REFERENCE_IMAGE_HEIGHT * scale);
    }
    private void setupLoginBoxLayout(double loginWidth, double loginHeight, double padding, double spacing){
        loginBox.setPrefWidth(loginWidth);
        loginBox.setPrefHeight(loginHeight);
        loginBox.setMaxWidth(loginWidth);
        loginBox.setMaxHeight(loginHeight);
        loginBox.setPadding(new Insets(padding));
        loginBox.setSpacing(spacing);
    }
    private void setupTextfieldsLayout(double fieldWidth){
        emailField.setPrefWidth(fieldWidth);
        emailField.setMaxWidth(fieldWidth);

        passwordField.setPrefWidth(fieldWidth);
        passwordField.setMaxWidth(fieldWidth);
        passwordContainer.setPrefWidth(fieldWidth);

        passwordContainer.setMaxWidth(fieldWidth);
        passwordTextField.setPrefWidth(fieldWidth);
        passwordTextField.setMaxWidth(fieldWidth);
    }
    private void setupLabelsLayout(double welcomeTextScale, double accessButtonScale, double warningTextScale, double registerScale){
        welcomeText.setStyle("-fx-font-size: " + ((15 * welcomeTextScale * 2) + 2) + "px;");
        access.setStyle("-fx-font-size: " + (15 * accessButtonScale * 2) + "px;");
        warningText.setStyle("-fx-font-size: " + (warningTextScale) + "px;");
        register.setStyle("-fx-font-size: " + (registerScale) + "px;");
        resetPassword.setStyle("-fx-font-size: " + (registerScale) + "px;");
    }
    private void setupVBoxLayout(double verticalMargin){
        VBox.setMargin(welcomeText, new Insets(0, 0, verticalMargin * 3, 0));
        VBox.setMargin(emailField, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
        VBox.setMargin(passwordContainer, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
    }
    private void adjustLayout(double width, double height){
        //Adjust the scale to provide a more gradual scaling curve instead of linear scaling
        double scale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);

        scale = 1 - (1 - scale) * 0.5;

        if (imageContainer != null){
            boolean showImage = width > IMAGE_VISIBILITY_THRESHOLD;
            imageContainer.setVisible(showImage);
            imageContainer.setManaged(showImage);
            if (showImage){
                setupImageContainerLayout(scale);
            }
        }

        if (loginBox != null){

            boolean showLoginBox = width > MIN_VBOX_VISIBILITY_THRESHOLD;
            loginBox.setVisible(showLoginBox);
            loginBox.setManaged(showLoginBox);
            /*COMPACT_MODE and the MIN_VBOX_VISIBILITY_THRESHOLD are used to set the scene into a smaller one
            to provide the responsive layout shadowing the MOVIESTAR logo*/
            if (showLoginBox){
                boolean compactMode = width < COMPACT_MODE_THRESHOLD;
                double loginWidth = compactMode ? 280 : REFERENCE_LOGIN_WIDTH * scale;
                double loginHeight = compactMode ? 300: REFERENCE_LOGIN_HEIGHT * scale;
                double padding = Math.max(10, 20 * scale);
                double spacing = Math.max(5, 10 * scale);

                setupLoginBoxLayout(loginWidth, loginHeight, padding, spacing);
                StackPane.setAlignment(loginBox, compactMode ? Pos.CENTER : Pos.CENTER_RIGHT);
                StackPane.setMargin(loginBox, compactMode ? new Insets(0): new Insets(0, Math.max(50, (140 * scale)), 0, 0));

                double baseFontSize = 15 * scale;
                double welcomeTextScale = Math.max(scale, 0.7);
                double accessButtonScale = Math.max(scale, 0.7);
                double warningTextScale = Math.max(baseFontSize, 0.7);
                double registerScale = Math.max(baseFontSize, 0.7);

                setupLabelsLayout(welcomeTextScale, accessButtonScale, warningTextScale, registerScale);

                double fieldWidth = Math.min(loginWidth - padding * 2, loginWidth * 0.9);
                setupTextfieldsLayout(fieldWidth);

                double verticalMargin = 10 * scale;
                setupVBoxLayout(verticalMargin);
            }
        }
    }
    //Function to switch to registration page if u don't have an account
    private void switchToRegistrationPage(){
        try{

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/register.fxml"), Main.resourceBundle);
            Parent registerContent = loader.load();
            Scene currentScene = mainContainer.getScene();
            Scene newScene = new Scene(registerContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(newScene);
        }
        catch(IOException e ){
            warningText.setText("Loading error: " + e.getMessage());
        }
    }
    //General input text checks
    private boolean check_access(String _email, String _password) {
        if (_email.isEmpty() || _password.isEmpty()) {
            warningText.setText("Add email and password");
            AnimationUtils.shake(warningText);
            return false;
        }
        return true;
    }

    private String getCurrentPassword(){

        return isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
    }
    //Function that sends a verification code to reset the user password
    private void sendCredential() throws SQLException, IOException,MessagingException{
        String email = emailField.getText();
        AccountDao dao = new AccountDao();

        if(dao.searchAccount(email) == null){
            warningText.setText("Insert your email");
            AnimationUtils.shake(warningText);
        }
        else{
            StringBuilder sb = new StringBuilder(6);
            Random random = new Random();
            String cifre = "0123456789";
            for (int i = 0; i < 6; i++){
                int randomIndex = random.nextInt(cifre.length());
                char randomDigit = cifre.charAt(randomIndex);
                sb.append(randomDigit);
            }

            String verificationCode = sb.toString();

            String subject = "Your MovieStar Password Reset Code";
            String body = String.format("""
                Hello,
                
                We received a request to reset your MovieStar account password.
                
                Your verification code is: %s
                
                If you did not request this, please ignore this message.
                
                Thank you,
                The MovieStar Team
                """, verificationCode);

            try{
                emailService.sendEmail(email, subject, body );
                warningText.setText("Verification code sent to your email");
                AnimationUtils.pulse(warningText);
            }
            catch(MessagingException e ){
                warningText.setText("Failed to send email. Please try again.");
                AnimationUtils.shake(warningText);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/reset-password-view.fxml"), Main.resourceBundle);
            Parent resetContent = loader.load();
            Node currentContent = mainContainer.getChildren().getFirst();
            AnimationUtils.fadeOut(currentContent, 100);

            Scene currentScene = mainContainer.getScene();
            Scene newScene = new Scene(resetContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage)mainContainer.getScene().getWindow();
            stage.setScene(newScene);
            ResetController resetController = loader.getController();
            resetController.setUserEmail(email);
            resetController.setVerificationCodeStartTimer(verificationCode);
            resetController.setupResetButton();
        }
    }

    private void loginUser() throws SQLException{
        String email = emailField.getText();
        String password = getCurrentPassword();
        if(!check_access(email, password))
            return;

        try{
            AccountDao dao = new AccountDao();
            AnimationUtils.pulse(access);
            Account temp_acc = dao.searchAccount(email);

            if(temp_acc == null){
                emailField.setText("");
                passwordField.setText("");
                passwordTextField.setText("");
                warningText.setText("Account does not exist");
                AnimationUtils.shake(warningText);
                return;
            }

            // Use BCrypt to verify the password
            if(CredentialCryptManager.verifyPassword(password, temp_acc.getPassword())){
                this.account = temp_acc;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"), Main.resourceBundle);
                Parent homeContent = loader.load();
                ProfileView profileView = loader.getController();
                profileView.setAccount(account);
                Scene currentScene = mainContainer.getScene();
                Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());
                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.setScene(newScene);
            }
            else{
                emailField.setText("");
                passwordField.setText("");
                passwordTextField.setText("");
                warningText.setText("Wrong password");
                AnimationUtils.shake(warningText);
            }
        }
        catch(IOException e ){
            warningText.setText("Loading error: " + e.getMessage());
        }
        catch(Exception e ){
            warningText.setText("An error occurred: " + e.getMessage());
        }
    }
    public void setAccount(Account account){
        this.account = account;
        System.out.println("Access : email " + account.getEmail());
    }
}