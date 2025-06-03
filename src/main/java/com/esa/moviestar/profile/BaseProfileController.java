package com.esa.moviestar.profile;

import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.model.Account;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public abstract class BaseProfileController {

    @FXML
    protected GridPane pageContainer;
    @FXML
    protected VBox elementContainer;
    @FXML
    protected Group defaultImagine;
    @FXML
    protected Label creationTitle;
    @FXML
    protected TextField textName;
    @FXML
    protected HBox imageScroll1;
    @FXML
    protected HBox imageScroll2;
    @FXML
    protected HBox imageScroll3;
    @FXML
    protected HBox imageScroll4;
    @FXML
    protected VBox scrollContainer;
    @FXML
    protected Button saveButton;
    @FXML
    protected Button cancelButton;
    @FXML
    protected Label warningText;
    @FXML
    protected Label errorText;
    @FXML
    protected VBox imageContainer;

    protected Group originalProfileImage;
    protected int codCurrentImage;
    protected Account account;

    protected static final int MAX_LENGTH_NAME = 10;
    protected static final double SVG_SCALE = 3.8;
    protected static final double SVG_SCALE_HOVER = 4.0;
    protected static final int HBOX_SPACING = 130;
    protected static final int DEFAULT_SVG = 10;

    public void setAccount(Account account) {
        this.account = account;
        System.out.println(getClass().getSimpleName() + " = email : " + account.getEmail());
    }

    protected boolean validateName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    protected boolean validateNameLength(String name) {
        return name.length() < MAX_LENGTH_NAME;
    }

    public void initialize() {
        setUI();
        createImageGallery();
        setButtons();
        onInitializeComplete();
    }

    protected void setUI() {
        errorText.setText("");

        codCurrentImage = getInitialImageCode();
        defaultImagine.getChildren().clear();
        defaultImagine.getChildren().add(IconSVG.takeElement(codCurrentImage));
        defaultImagine.setScaleX(DEFAULT_SVG);
        defaultImagine.setScaleY(DEFAULT_SVG);

        creationTitle.setText(getTitleText());
        textName.setPromptText("Name");

        scrollContainer.setSpacing(90);

        saveButton.setText("Save");
        cancelButton.setText("Cancel");
    }

    protected void createImageGallery() {
        for (int i = 0; i <= 16; i++) {
            Group g = new Group();
            g.setScaleX(SVG_SCALE);
            g.setScaleY(SVG_SCALE);

            g.setOnMouseEntered(event -> {
                g.setScaleY(SVG_SCALE_HOVER);
                g.setScaleX(SVG_SCALE_HOVER);
            });
            g.setOnMouseExited(event -> {
                g.setScaleX(SVG_SCALE);
                g.setScaleY(SVG_SCALE);
            });

            g.getChildren().add(IconSVG.takeElement(i));

            // Distribuzione delle icone tra gli HBox
            if (i <= 3) {
                imageScroll1.getChildren().add(g);
            } else if (i <= 7) {
                imageScroll2.getChildren().add(g);
            } else if (i <= 11) {
                imageScroll3.getChildren().add(g);
            } else if (i <= 15) {
                imageScroll4.getChildren().add(g);
            }
        }

        imageScroll1.setSpacing(HBOX_SPACING);
        imageScroll2.setSpacing(HBOX_SPACING);
        imageScroll3.setSpacing(HBOX_SPACING);
        imageScroll4.setSpacing(HBOX_SPACING);

        setupImageProfile(imageScroll1);
        setupImageProfile(imageScroll2);
        setupImageProfile(imageScroll3);
        setupImageProfile(imageScroll4);
    }

    protected void setButtons() {
        cancelButton.setOnMouseClicked(e -> handleCancel());
        saveButton.setOnMouseClicked(event -> handleSave());
    }

    protected void handleSave() {
        String name = textName.getText();

        if (!validateName(name)) {
            showError("No name entered");
            return;
        }

        if (!validateNameLength(name)) {
            showError("Name too long (max " + MAX_LENGTH_NAME + " characters)");
            return;
        }

        if (performSave(name, codCurrentImage)) {
            onSaveSuccess();
        } else {
            warningText.setText("Error saving the profile.");
        }
    }

    protected void showError(String message) {
        errorText.setText(message);
        AnimationUtils.shake(errorText);
    }

    protected void setupImageProfile(HBox imageScroll) {
        for (int i = 0; i < imageScroll.getChildren().size(); i++) {
            Node scrollImage = imageScroll.getChildren().get(i);
            scrollImage.setOnMouseClicked(event -> handleImageSelection(scrollImage));
        }
    }

    protected void handleImageSelection(Node scrollImage) {
        Group originalGroup = (Group) scrollImage;
        Group clonedGroup = IconSVG.copyGroup(originalGroup);

        defaultImagine.getChildren().clear();
        defaultImagine.getChildren().addFirst(clonedGroup);

        originalProfileImage = clonedGroup;

        // Calcola l'indice dell'immagine selezionata
        for (int j = 0; j < 4; j++) {
            HBox c = (HBox) imageContainer.getChildren().get(j);
            if (c.getChildren().contains(originalGroup)) {
                codCurrentImage = c.getChildren().indexOf(originalGroup) + j * 4;
                break;
            }
        }
    }

    // Metodi astratti che devono essere implementati dalle classi figlie
    protected abstract String getTitleText();
    protected abstract int getInitialImageCode();
    protected abstract void handleCancel();
    protected abstract boolean performSave(String name, int imageCode);
    protected abstract void onSaveSuccess();
    protected abstract void onInitializeComplete();
}