package com.esa.moviestar.home;

import com.esa.moviestar.database.UtenteDao;
import com.esa.moviestar.components.PopupMenu;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;
import java.util.ResourceBundle;


public class HeaderController {
    private PopupMenu popupMenu;
    private final ResourceBundle resources = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    @FXML
    private StackPane rootHeader;
    @FXML
    private HBox titleImageContainer;

    @FXML
    StackPane homeButton;

    @FXML
    HBox filmButton;

    @FXML
    HBox seriesButton;

    @FXML
    HBox searchButton;

    @FXML
    private TextField tbxSearch;

    private Node currentActive;
    @FXML
    private StackPane profileImage;

    @FXML
    public void initialize() {
        currentActive = homeButton;
        currentActive.getStyleClass().remove("surface-transparent");
        currentActive.getStyleClass().add("primary");
        tbxSearch.setPromptText("Titles, persons, genres");
        profileImage.setOnMouseClicked(e -> popupMenu.show(profileImage));
        rootHeader.widthProperty().addListener((observable, oldValue, newValue) -> {
            titleImageContainer.setVisible(!(newValue.doubleValue() < 720));
        });
        tbxSearch.onMouseClickedProperty().bindBidirectional(searchButton.onMouseClickedProperty());
    }

    public void activeSearch() {
        if(currentActive==searchButton)
            return;
        currentActive.getStyleClass().remove("primary");
        currentActive.getStyleClass().add("surface-transparent");
        currentActive=searchButton;
        searchButton.getStyleClass().remove("surface-transparent");
        searchButton.getStyleClass().add("surface-dim");
    }

    public void setUpPopUpMenu(MainPagesController father, Utente user, Account account){
        UtenteDao utenteDao = new UtenteDao();
        List<Utente> users = utenteDao.recuperaTuttiGliUtenti(user.getEmail());
        setupPopupMenu(father,account,user,users);
    }
    public TextField getTbxSearch(){
        return tbxSearch;
    }

    /**
     * Sets the visual style for the active button and resets the previous one.
     *
     * @param button The Node (StackPane or HBox) that was clicked.
     */
    public void activeButton(Node button) {
        // If the clicked button is already the active one, do nothing
        if (button == currentActive) {
            return;
        }

        // Deactivate the previously active button
        if (currentActive != null) {
            if (currentActive == searchButton) {
                searchButton.getStyleClass().remove("surface-dim");
                searchButton.getStyleClass().add("surface-transparent");
            }else{
                currentActive.getStyleClass().remove("primary");
                // Ensure the inactive style is present
                if (!currentActive.getStyleClass().contains("surface-transparent")) {
                    currentActive.getStyleClass().add("surface-transparent");
                }
            }
        }

        // Activate the new button
        currentActive = button;
        if (currentActive != null) {
            currentActive.getStyleClass().remove("surface-transparent"); // *** Important: Remove inactive style ***
            // Ensure the active style is present
            if (!currentActive.getStyleClass().contains("primary")) {
                currentActive.getStyleClass().add("primary");
            }
            // If the activated element is the search HBox containing the TextField,
            // you might want to give focus to the TextField itself.
            if (currentActive == searchButton) {
                tbxSearch.requestFocus();
            }
        }
    }


    private void setupPopupMenu(MainPagesController father,Account account, Utente user, List<Utente> users) {
        // Create the popup menu - no stage needed
        popupMenu = new PopupMenu();

        // Add menu items
        HBox settingsItem = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().addAll("small-item", "chips", "surface-transparent");
        }};
        SVGPath profileIcon = new SVGPath() {{
            setContent(resources.getString("user"));
            getStyleClass().add("on-primary");
        }};
        Text text = new Text("My Account") {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        settingsItem.getChildren().addAll(profileIcon, text);
        settingsItem.setOnMouseClicked(e -> {
                    father.settingsClick(user,account);
                    popupMenu.close();
                }
        );

        popupMenu.addItem(settingsItem);
        popupMenu.addSeparator();
        for (Utente i : users) {
            if (user.getID()!=i.getID()) {
                createProfileItem(i, i.getNome(), i.getIcona(), father);
            }
        }
        if(users.size()<4){createAddItem(account,father);}
        popupMenu.addSeparator();
        HBox emailButton = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER);
            getStyleClass().addAll("small-item", "surface-dim-border", "chips", "surface-transparent");
        }};
        SVGPath logoutIcon = new SVGPath() {{
            setContent(resources.getString("logout"));
            getStyleClass().add("on-primary");
        }};
        Text logoutText = new Text("Logout") {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        emailButton.setOnMouseClicked(e -> {father.emailClick();popupMenu.close();});
        emailButton.getChildren().addAll(logoutIcon, logoutText);
        popupMenu.addItem(emailButton);

    }

    private void createProfileItem(Utente user, String name, Group profileIcon, MainPagesController father) {
        profileIcon.setScaleX(1.3);
        profileIcon.setScaleY(1.3);
        HBox item = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().addAll("small-item", "chips", "surface-transparent");
        }};
        Text text = new Text(name) {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        item.getChildren().addAll(profileIcon, text);
        // Item click handling moved to controller via callback
        item.setOnMouseClicked(e -> {father.profileClick(user);popupMenu.close();});
        popupMenu.addItem(item);

    }



    public void setProfileIcon(Group icon) {
        profileImage.getChildren().clear();
        icon.setScaleX(2.286);
        icon.setScaleY(2.286);
        profileImage.getChildren().add(icon);
    }

    private void createAddItem(Account account , MainPagesController father) {
        SVGPath cross = new SVGPath(){{
            setContent(resources.getString("plusButton"));
            getStyleClass().add("on-primary");
        }};
        HBox item = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().addAll("small-item", "chips", "surface-transparent");
        }};
        Text text = new Text("New user") {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        item.getChildren().addAll(cross, text);
        // Item click handling moved to controller via callback
        item.setOnMouseClicked(e -> {father.createProfileUser(account);popupMenu.close();});
        popupMenu.addItem(item);

    }



}