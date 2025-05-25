package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.Main;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.model.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
    private Label warning1;
    @FXML
    private Label warning2;
    @FXML
    private Label warningSpecial;
    @FXML
    private StackPane ContenitorePadre;
    @FXML
    private Button backToLogin;
    @FXML
    private VBox registerBox;
    @FXML
    private ImageView titleImage;
    @FXML
    private VBox ContenitoreImmagine;

    // database access
    //private UserDatabase userDatabase;

    // Regex patterns for email and password validation
    private String email_regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private String password_regex = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\-])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+{}\\[\\]:;<>,.?~\\-]{8,}$";



    // Valori di riferimento per il layout responsivo
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_REGISTER_WIDTH = 433.0;
    private final double REFERENCE_REGISTER_HEIGHT = 560.0;
    private final double REFERENCE_IMAGE_WIDTH = 700.0;
    private final double REFERENCE_IMAGE_HEIGHT = 194.0;
    private final double MIN_SCREEN_WIDTH = 600.0;
    private final double MIN_SCREEN_HEIGHT = 700.0;

    /**
     * Initializes the controller. This method is automatically called after the FXML
     * has been loaded and the controller is fully initialized.
     */
    public void initialize() {
        // Initialize the database connection
        //userDatabase = new UserDatabase();
        // Set default prompts for email and password fields
        emailField.setPromptText("Email");
        passwordField.setPromptText("Password");
        ContenitorePadre.setMinWidth(1080);
        ContenitorePadre.setMinHeight(700);
        // Set event handler for the register button
        register.setOnAction(event -> saveUser());

        // Set event handler for the back to login button
        backToLogin.setOnAction(event -> switchToLoginPage());

        // Display password requirements
        warning1.setText("Password con almeno :");
        warning2.setText("8 caratteri");
        warningSpecial.setText("1 carattere speciale e 1 numero");

        // Apply animations sequentially
        Node[] formElements = {welcomeText, emailField, passwordField, warning1, warningSpecial, warning2, register, backToLogin};
        AnimationUtils.animateSimultaneously(formElements, 1);

        // Configura il layout responsivo
        setupResponsiveLayout();
    }

    private void setupResponsiveLayout() {
        ContenitorePadre.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((observable, oldValue, newValue) -> adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((observable, oldValue, newValue) -> adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                adjustLayout(newScene.getWidth(), newScene.getHeight()); // Adattamento iniziale
            }
        });
    }

    private void adjustLayout(double width, double height) {
        // Fattore di scala basato sulla dimensione MINORE
        double rawScale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        double scale = 1 - (1 - rawScale) * 0.5; // Applica smorzamento del 50%

        // Gestione dell'immagine (invariato)
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
                backToLogin.setStyle("-fx-font-size: " + (backToLoginScale * 12 + 2) + "px;");

                // Calcola la larghezza disponibile per gli elementi
                double availableWidth = registerWidth - paddingValue * 2; // Larghezza interna al padding
                double fieldWidth = Math.min(availableWidth, registerWidth * 0.9); // Larghezza massima dei campi

                emailField.setPrefWidth(fieldWidth);
                passwordField.setPrefWidth(fieldWidth);
                emailField.setMaxWidth(fieldWidth);
                passwordField.setMaxWidth(fieldWidth);

                double buttonWidth = Math.min(availableWidth, registerWidth * 0.6);
                buttonWidth = Math.max(buttonWidth, 100);

                register.setPrefWidth(buttonWidth);
                register.setMaxWidth(buttonWidth);

                backToLogin.setMaxWidth(availableWidth);
                backToLogin.setWrapText(true);

                // Margini dinamici
                double verticalMargin = 10 * scale;
                VBox.setMargin(welcomeText, new Insets(0, 0, verticalMargin * 3, 0));
                VBox.setMargin(emailField, new Insets(0, 0, verticalMargin * 2, 0));
                VBox.setMargin(passwordField, new Insets(0, 0, verticalMargin * 2, 0));
                VBox.setMargin(warning1, new Insets(0, 0, verticalMargin, 0));
                VBox.setMargin(warning2, new Insets(0, 0, verticalMargin, 0));
                VBox.setMargin(warningSpecial, new Insets(0, 0, verticalMargin * 2, 0));
                VBox.setMargin(register, new Insets(0, 0, verticalMargin * 3, 0));
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

            Scene currentScene = ContenitorePadre.getScene();
            Scene newScene = new Scene(loginContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) ContenitorePadre.getScene().getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {

            System.err.println("Register: Access page error to load" + e.getMessage());

        }
    }

    /**
     * Saves a new user to the database after validating the email and password.
     */

    public String get_regex(){
        return password_regex;
    }

    private void saveUser() {
        // Retrieve email and password from input fields
        String email = emailField.getText();
        String password = passwordField.getText();

        if (!Pattern.matches(email_regex, email) && !Pattern.matches(password_regex, password)) {
            AnimationUtils.shake(emailField);
            AnimationUtils.shake(passwordField);
            emailField.setPromptText("Email non valida");
            emailField.setText("");
            passwordField.setPromptText("Password non valida");
            passwordField.setText("");
            return;
        }

        // Validate email using regex
        if (!Pattern.matches(email_regex, email)) {
            AnimationUtils.shake(emailField);
            emailField.setPromptText("Email non valida");
            emailField.setText("");
            return;
        }

        // Validate password using regex
        if (!Pattern.matches(password_regex, password)) {
            AnimationUtils.shake(passwordField);
            passwordField.setPromptText("Password non valida");
            passwordField.setText("");
            return;
        }

        // Show pulse animation on register button to indicate processing
        AnimationUtils.pulse(register);

        // Create a new User object


        // Attempt to add the user to the database
        try {
            Account account = new Account(email,password);
            AccountDao dao = new AccountDao();
            if (dao.inserisciAccount(account)) {
                welcomeText.setText("Utente registrato con successo!");
                AnimationUtils.pulse(welcomeText);

                //Subito dopo la registrazione carica la pagina con la creazione del profilo
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);
                Parent homeContent = loader.load();
                CreateProfileController createProfileController = loader.getController();
                createProfileController.setAccount(account);

                // Ottieni la scena corrente
                Scene currentScene = ContenitorePadre.getScene();

                // Crea una nuova scena con il nuovo contenuto
                Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());

                // Ottieni lo Stage corrente e imposta la nuova scena
                Stage stage = (Stage) ContenitorePadre.getScene().getWindow();
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
