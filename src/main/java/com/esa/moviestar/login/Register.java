package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.Main;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.model.Account;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// BCrypt import
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.regex.Pattern;

public class Register {

    // FXML injected UI components
    @FXML
    private Label welcomeText;
    @FXML
    private Button register;
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
    private Label warning1;
    @FXML
    private Label warning2;
    @FXML
    private Label warningSpecial;
    @FXML
    private Label warningSpecial2;
    @FXML
    private StackPane mainContainer;
    @FXML
    private Button backToLogin;
    @FXML
    private VBox registerBox;
    @FXML
    private ImageView titleImage;
    @FXML
    private VBox imageContainer;

    private boolean isPasswordVisible = false;

    // Regex patterns for email and password validation
    private String email_regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private String password_regex = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\-])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+{}\\[\\]:;<>,.?~\\-]{8,}$";

    private static final int BCRYPT_ROUNDS = 12;

    // Valori di riferimento per il layout responsivo
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_REGISTER_WIDTH = 433.0;
    private final double REFERENCE_REGISTER_HEIGHT = 560.0;
    private final double REFERENCE_IMAGE_WIDTH = 700.0;
    private final double REFERENCE_IMAGE_HEIGHT = 194.0;
    private final double MIN_SCREEN_WIDTH = 600.0;
    private final double MIN_SCREEN_HEIGHT = 700.0;


    public void initialize() {
        // Set default prompts for email and password fields
        emailField.setPromptText("Email");
        passwordField.setPromptText("Password");
        passwordTextField.setPromptText("Password");
        mainContainer.setMinWidth(1080);
        mainContainer.setMinHeight(700);

        // Setup password toggle functionality
        setupPasswordToggle();

        // Set event handler for the register button
        register.setOnAction(event -> saveUser());

        // Set event handler for the back to login button
        backToLogin.setOnAction(event -> switchToLoginPage());

        // Display password requirements
        warning1.setText("Password must contain at least:");
        warning2.setText("   · 8 characters");
        warningSpecial.setText("   · 1 number");
        warningSpecial2.setText("   · 1 special character");
        welcomeText.setText("SIGN UP");
        backToLogin.setText("Already have an account? Sign in");
        register.setText("Register");

        Node[] formElements = {welcomeText, emailField, passwordField, warning1, warningSpecial, warningSpecial2, warning2, register, backToLogin};
        AnimationUtils.animateSimultaneously(formElements, 1);

        setupResponsiveLayout();
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

        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordButton, new Insets(0, 10, 0, 0));

        syncPasswordFieldWidths();

        // Add listener to maintain width sync when email field changes
        emailField.widthProperty().addListener((obs, oldWidth, newWidth) -> syncPasswordFieldWidths());
        emailField.prefWidthProperty().addListener((obs, oldWidth, newWidth) -> syncPasswordFieldWidths());
    }

    private void syncPasswordFieldWidths() {
        // Get email field dimensions
        double emailWidth = emailField.getWidth();
        double emailPrefWidth = emailField.getPrefWidth();
        double emailMinWidth = emailField.getMinWidth();
        double emailMaxWidth = emailField.getMaxWidth();

        // Apply same dimensions to password fields
        passwordField.setPrefWidth(emailPrefWidth);
        passwordField.setMinWidth(emailMinWidth);
        passwordField.setMaxWidth(emailMaxWidth);

        passwordTextField.setPrefWidth(emailPrefWidth);
        passwordTextField.setMinWidth(emailMinWidth);
        passwordTextField.setMaxWidth(emailMaxWidth);

        // Set container width to match email field
        passwordContainer.setPrefWidth(emailPrefWidth);
        passwordContainer.setMinWidth(emailMinWidth);
        passwordContainer.setMaxWidth(emailMaxWidth);
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
            togglePasswordButton.setText("🙈");
        }

        else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setText("👁");
        }
    }

    private String getCurrentPassword() {
        return isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
    }

    private void setupResponsiveLayout() {
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((observable, oldValue, newValue) -> adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((observable, oldValue, newValue) -> adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                adjustLayout(newScene.getWidth(), newScene.getHeight()); // Adattamento iniziale
            }
        });
    }

    private void adjustLayout(double width, double height) {

        double rawScale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        double scale = 1 - (1 - rawScale) * 0.5; // Applica smorzamento del 50%

        if (titleImage != null) {

            boolean showImage = width > MIN_SCREEN_WIDTH;

            titleImage.setVisible(showImage);
            titleImage.setManaged(showImage);
            if (showImage) {

                titleImage.setFitWidth(REFERENCE_IMAGE_WIDTH * scale);
                titleImage.setFitHeight(REFERENCE_IMAGE_HEIGHT * scale);
                VBox.setMargin(titleImage, new Insets(((scale + 10) * 0.85),0,0,0));

            }
        }

        // Gestione del registerBox
        if (registerBox != null) {
            boolean showRegisterBox = width > MIN_SCREEN_WIDTH / 2;
            registerBox.setVisible(showRegisterBox);
            registerBox.setManaged(showRegisterBox);

            if (showRegisterBox) {
                boolean compactMode = width < MIN_SCREEN_WIDTH;
                double registerWidth = compactMode ? 280 : REFERENCE_REGISTER_WIDTH * scale;

                // Altezza minima di BASE (percentuale fissa dell'altezza di riferimento)
                double baseMinHeightPercentage = 0.75; // Prova con 75%
                double baseMinRegisterHeight = REFERENCE_REGISTER_HEIGHT * baseMinHeightPercentage;

                // BLOCCO Altezza per larghezza stretta
                double narrowWidthThreshold = 700.0; // Soglia di larghezza per attivare il blocco
                double lockedMinRegisterHeight = 500.0; // Altezza minima fissa quando la larghezza è stretta

                double calculatedRegisterHeight = (REFERENCE_REGISTER_HEIGHT * scale)+25;

                double registerHeight;
                if (width < narrowWidthThreshold) {
                    registerHeight = Math.max(calculatedRegisterHeight, lockedMinRegisterHeight);
                } else {
                    registerHeight = Math.max(calculatedRegisterHeight, baseMinRegisterHeight);
                }

                registerBox.setPrefWidth(registerWidth);
                registerBox.setPrefHeight(registerHeight);
                registerBox.setMaxWidth(registerWidth);
                registerBox.setMaxHeight(registerHeight);

                // Padding e Spacing dinamici
                double paddingValue = Math.max(10, 20 * scale); // Rinominato per chiarezza
                double spacing = Math.max(5, 10 * scale);
                registerBox.setPadding(new Insets(paddingValue));
                registerBox.setSpacing(spacing);

                // --- IMPOSTAZIONE FONT (prima di usarli per calcoli) ---
                double welcomeTextScale = Math.max(scale, 0.7);
                double registerButtonScale = Math.max(scale, 0.6);
                double warningTextScale = Math.max(scale, 0.7);
                double backToLoginScale = Math.max(scale, 0.7);

                // Imposta stile per welcomeText PRIMA di calcolarne la larghezza
                String welcomeTextStyle = "-fx-font-size: " + (15 * welcomeTextScale * 2) + "px;";
                welcomeText.setStyle(welcomeTextStyle);

                // Imposta stile per bottone register
                String registerButtonStyle = "-fx-font-size: " + (15 * registerButtonScale * 2) + "px;";
                register.setStyle(registerButtonStyle);

                // Imposta stili per altri elementi
                warning1.setStyle("-fx-font-size: " + (warningTextScale * 12 + 2) + "px;");
                warning2.setStyle("-fx-font-size: " + (warningTextScale * 12 + 2) + "px;");
                warningSpecial.setStyle("-fx-font-size: " + (warningTextScale * 12 + 2) + "px;");
                warningSpecial2.setStyle("-fx-font-size: " + (warningTextScale * 12 + 2) + "px;");
                backToLogin.setStyle("-fx-font-size: " + (backToLoginScale * 12 + 2) + "px;");

                // Calcola la larghezza disponibile per gli elementi
                double availableWidth = registerWidth - paddingValue * 2; // Larghezza interna al padding
                double fieldWidth = Math.min(availableWidth, registerWidth * 0.9); // Larghezza massima dei campi

                // Set email field width first
                emailField.setPrefWidth(fieldWidth);
                emailField.setMaxWidth(fieldWidth);

                // Then sync password fields to match email field
                passwordField.setPrefWidth(fieldWidth);
                passwordTextField.setPrefWidth(fieldWidth);
                passwordContainer.setPrefWidth(fieldWidth);
                passwordField.setMaxWidth(fieldWidth);
                passwordTextField.setMaxWidth(fieldWidth);
                passwordContainer.setMaxWidth(fieldWidth);

                // Force synchronization after layout changes
                syncPasswordFieldWidths();

                double buttonWidth = Math.min(availableWidth, registerWidth * 0.6);
                buttonWidth = Math.max(buttonWidth, 100);

                register.setPrefWidth(buttonWidth);
                register.setMaxWidth(buttonWidth);

                backToLogin.setMaxWidth(availableWidth);
                backToLogin.setWrapText(true);

                // Dynamic margin
                double verticalMargin = 10 * scale;
                VBox.setMargin(welcomeText, new Insets(0, 0, verticalMargin * 3, 0));
                VBox.setMargin(emailField, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
                VBox.setMargin(passwordContainer, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
                VBox.setMargin(warning1, new Insets(0, 0, verticalMargin, 0));
                VBox.setMargin(warning2, new Insets(0, 0, verticalMargin, 0));
                VBox.setMargin(warningSpecial, new Insets(0, 0, verticalMargin , 0));
                VBox.setMargin(warningSpecial2, new Insets(0, 0, verticalMargin * 2, 0));
                VBox.setMargin(register, new Insets(0, 0, verticalMargin, 0));
                VBox.setMargin(backToLogin, new Insets(0, 0, 0, 0));
            }
        }
    }

    /**
     * Update the current page with login content
     */
    private void switchToLoginPage() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
            Parent loginContent = loader.load();

            Scene currentScene = mainContainer.getScene();
            Scene newScene = new Scene(loginContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(newScene);
        }
        catch (IOException e) {
            System.err.println("Register: Access page error to load" + e.getMessage());
        }
    }

    public String get_regex(){
        return password_regex;
    }

    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    private void saveUser() {

        String email = emailField.getText();
        String password = getCurrentPassword();

        if (!Pattern.matches(email_regex, email) && !Pattern.matches(password_regex, password)) {
            AnimationUtils.shake(emailField);
            AnimationUtils.shake(passwordContainer);
            emailField.setPromptText("Invalid email");
            emailField.setText("");

            passwordField.setPromptText("Invalid password");
            passwordField.setText("");

            passwordTextField.setPromptText("Invalid password");
            passwordTextField.setText("");

            return;
        }

        // Validate email using regex
        if (!Pattern.matches(email_regex, email)) {

            AnimationUtils.shake(emailField);

            emailField.setPromptText("Invalid email");
            emailField.setText("");

            return;
        }

        // Validate password using regex
        if (!Pattern.matches(password_regex, password)) {

            AnimationUtils.shake(passwordContainer);

            passwordField.setPromptText("Invalid password");
            passwordField.setText("");

            passwordTextField.setPromptText("Invalid password");
            passwordTextField.setText("");

            return;
        }
        AnimationUtils.pulse(register);

        try {

            String hashedPassword = hashPassword(password);

            // Create account with hashed password
            Account account = new Account(email, hashedPassword);
            AccountDao dao = new AccountDao();

            if (dao.inserisciAccount(account)) {
                welcomeText.setText("User registered successfully!");
                AnimationUtils.pulse(welcomeText);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);

                Parent homeContent = loader.load();
                CreateProfileController createProfileController = loader.getController();

                createProfileController.setAccount(account);

                Scene currentScene = mainContainer.getScene();
                Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.setScene(newScene);
            } else {
                welcomeText.setText("Utente già esistente");
                AnimationUtils.shake(welcomeText);
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}