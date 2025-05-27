package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.Main;
import com.esa.moviestar.profile.ProfileView;
import com.esa.moviestar.model.Account;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Random;

public class Access {

    @FXML
    private Label welcomeText;
    @FXML
    private Label warningText;
    @FXML
    private Button register;
    @FXML
    private Button access;
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
    private StackPane ContenitorePadre;
    @FXML
    private VBox loginBox;
    @FXML
    private Button recuperoPassword;
    @FXML
    private StackPane ContenitoreImmagine;

    private EmailService emailService;
    private Account account;
    private boolean isPasswordVisible = false;

    // Valori di riferimento
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_LOGIN_WIDTH = 400.0;
    private final double REFERENCE_LOGIN_HEIGHT = 459.0;
    private final double REFERENCE_IMAGE_WIDTH = 700.0;
    private final double REFERENCE_IMAGE_HEIGHT = 194.0;
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double IMAGE_VISIBILITY_THRESHOLD = 600.0;

    public void initialize() {
        emailService = new EmailService();
        emailField.setPromptText("Email");
        ContenitorePadre.setMinWidth(1080);
        ContenitorePadre.setMinHeight(700);
        passwordField.setPromptText("Password");
        passwordTextField.setPromptText("Password");
        warningText.setText("");
        emailField.setMinWidth(200);
        passwordField.setMinWidth(200);
        passwordTextField.setMinWidth(200);

        // Inizializza il toggle password
        setupPasswordToggle();

        register.setOnAction(event -> switchToRegistrationPage());
        access.setOnAction(event -> {
            try {
                loginUser();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        recuperoPassword.setOnAction(event -> {
            try {
                invioCredenziali();
            } catch (SQLException | IOException | MessagingException e) {
                throw new RuntimeException(e);
            }
        });

        Node[] formElements = {welcomeText, emailField, passwordField, access, register};
        AnimationUtils.animateSimultaneously(formElements, 1);

        setupResponsiveLayout();
        setupKeyboardNavigation();
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

    private void setupPasswordToggle() {
        // Sincronizza i testi dei due campi password
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

        // Gestisce il click del bottone toggle
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());

        // Mantieni la posizione del bottone
        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordButton, new Insets(0, 10, 0, 0));

        // Sync widths initially
        syncPasswordFieldWidths();

        // Add listener to maintain width sync when email field changes
        emailField.widthProperty().addListener((obs, oldWidth, newWidth) -> syncPasswordFieldWidths());
        emailField.prefWidthProperty().addListener((obs, oldWidth, newWidth) -> syncPasswordFieldWidths());
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Mostra la password come testo normale
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
            togglePasswordButton.setText("🙈"); // Occhio barrato
        } else {
            // Nasconde la password
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setText("👁"); // Occhio aperto
        }
    }

    private void setupKeyboardNavigation() {
        ContenitorePadre.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                access.fire();
            }
        });

        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isPasswordVisible) {
                    passwordTextField.requestFocus();
                } else {
                    passwordField.requestFocus();
                }
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                access.fire();
            }
        });

        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                access.fire();
            }
        });
    }

    private void setupResponsiveLayout() {
        ContenitorePadre.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((observable, oldValue, newValue) ->
                        adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                adjustLayout(newScene.getWidth(), newScene.getHeight());
            }
        });
    }

    private void adjustLayout(double width, double height) {
        double rawScale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        double scale = 1 - (1 - rawScale) * 0.5;

        // Gestione dell'immagine
        if (ContenitoreImmagine != null) {
            boolean showImage = width > IMAGE_VISIBILITY_THRESHOLD;
            ContenitoreImmagine.setVisible(showImage);
            ContenitoreImmagine.setManaged(showImage);
            if (showImage) {
                ContenitoreImmagine.setMinWidth(REFERENCE_IMAGE_WIDTH * scale);
                ContenitoreImmagine.setMinHeight(REFERENCE_IMAGE_HEIGHT * scale);
                ContenitoreImmagine.setPrefWidth(REFERENCE_IMAGE_WIDTH * scale);
                ContenitoreImmagine.setPrefHeight(REFERENCE_IMAGE_HEIGHT * scale);
                ContenitoreImmagine.setMaxWidth(REFERENCE_IMAGE_WIDTH * scale);
                ContenitoreImmagine.setMaxHeight(REFERENCE_IMAGE_HEIGHT * scale);
            }
        }

        // Gestione del login box
        if (loginBox != null) {
            boolean showLoginBox = width > MIN_VBOX_VISIBILITY_THRESHOLD;
            loginBox.setVisible(showLoginBox);
            loginBox.setManaged(showLoginBox);

            if (showLoginBox) {
                boolean compactMode = width < COMPACT_MODE_THRESHOLD;
                double loginWidth = compactMode ? 280 : REFERENCE_LOGIN_WIDTH * scale;
                double loginHeight = compactMode ? 300: REFERENCE_LOGIN_HEIGHT * scale;

                loginBox.setPrefWidth(loginWidth);
                loginBox.setPrefHeight(loginHeight);
                loginBox.setMaxWidth(loginWidth);
                loginBox.setMaxHeight(loginHeight);

                double padding = Math.max(10, 20 * scale);
                double spacing = Math.max(5, 10 * scale);
                loginBox.setPadding(new Insets(padding));
                loginBox.setSpacing(spacing);

                StackPane.setAlignment(loginBox, compactMode ? Pos.CENTER : Pos.CENTER_RIGHT);
                StackPane.setMargin(loginBox, compactMode ? new Insets(0) :
                        new Insets(0, Math.max(50, (140 * scale)), 0, 0));

                // Dimensioni font dinamiche
                double baseFontSize = 15 * scale;
                double welcomeTextScale = Math.max(scale, 0.7);
                double accessButtonScale = Math.max(scale, 0.7);
                double warningTextScale = Math.max(baseFontSize, 0.7);
                double registerScale = Math.max(baseFontSize, 0.7);

                welcomeText.setStyle("-fx-font-size: " + ((15 * welcomeTextScale * 2) + 2) + "px;");
                access.setStyle("-fx-font-size: " + (15 * accessButtonScale * 2) + "px;");
                warningText.setStyle("-fx-font-size: " + (warningTextScale) + "px;");
                register.setStyle("-fx-font-size: " + (registerScale) + "px;");
                recuperoPassword.setStyle("-fx-font-size: " + (registerScale) + "px;");

                double fieldWidth = Math.min(loginWidth - padding * 2, loginWidth * 0.9);

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

                double verticalMargin = 10 * scale;
                VBox.setMargin(welcomeText, new Insets(0, 0, verticalMargin * 3, 0));
                VBox.setMargin(emailField, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
                VBox.setMargin(passwordContainer, new Insets(0, 0, (verticalMargin * 2) + 10, 0));
            }
        }
    }

    private void switchToRegistrationPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/register.fxml"), Main.resourceBundle);
            Parent registerContent = loader.load();
            Scene currentScene = ContenitorePadre.getScene();
            Scene newScene = new Scene(registerContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) ContenitorePadre.getScene().getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            warningText.setText("Errore di caricamento: " + e.getMessage());
        }
    }

    private boolean check_access(String _email, String _password) {
        if (_email.isEmpty() || _password.isEmpty()) {
            warningText.setText("Inserisci email e password");
            AnimationUtils.shake(warningText);
            return false;
        }
        return true;
    }

    private String getCurrentPassword() {
        return isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
    }

    private void invioCredenziali() throws SQLException, IOException, MessagingException {
        String email = emailField.getText();
        AccountDao dao = new AccountDao();

        if (dao.cercaAccount(email) == null) {
            warningText.setText("Nessun account trovato per questa email.");
            AnimationUtils.shake(warningText);
        } else {
            StringBuilder sb = new StringBuilder(6);
            Random random = new Random();
            String cifre = "0123456789";
            for (int i = 0; i < 6; i++) {
                int randomIndex = random.nextInt(cifre.length());
                char randomDigit = cifre.charAt(randomIndex);
                sb.append(randomDigit);
            }
            String verificationCode = sb.toString();

            String subject = "Your MovieStar Password Reset Code";
            String body = """
            Hello,
            
            We received a request to reset your MovieStar account password.
            
            Your verification code is: 123456
            
            If you did not request this, please ignore this message.
            
            Thank you,
            The MovieStar Team
            """;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/reset-password-view.fxml"), Main.resourceBundle);
            Parent resetContent = loader.load();
            Node currentContent = ContenitorePadre.getChildren().getFirst();
            AnimationUtils.fadeOut(currentContent, 100);

            Scene currentScene = ContenitorePadre.getScene();
            Scene newScene = new Scene(resetContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) ContenitorePadre.getScene().getWindow();
            stage.setScene(newScene);

            ResetController resetController = loader.getController();
            resetController.setUserEmail(email);
            resetController.setVerificationCode(verificationCode);
            resetController.setupResetButton();
        }
    }

    private void loginUser() throws SQLException {
        String email = emailField.getText();
        String password = getCurrentPassword();

        if (!check_access(email, password)) {
            return;
        }

        try {
            AccountDao dao = new AccountDao();
            AnimationUtils.pulse(access);
            Account temp_acc = dao.cercaAccount(email);

            if (temp_acc == null) {
                emailField.setText("");
                passwordField.setText("");
                passwordTextField.setText("");
                warningText.setText("Account inesistente");
                AnimationUtils.shake(warningText);
                return;
            }

            if (Objects.equals(temp_acc.getPassword(), password)) {
                this.account = temp_acc;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"), Main.resourceBundle);
                Parent homeContent = loader.load();
                ProfileView profileView = loader.getController();
                profileView.setAccount(account);

                Scene currentScene = ContenitorePadre.getScene();
                Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());
                Stage stage = (Stage) ContenitorePadre.getScene().getWindow();
                stage.setScene(newScene);
            } else {
                emailField.setText("");
                passwordField.setText("");
                passwordTextField.setText("");
                warningText.setText("Password errata");
                AnimationUtils.shake(warningText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            warningText.setText("Errore di caricamento: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            warningText.setText("Errore durante l'accesso");
        }
    }
}