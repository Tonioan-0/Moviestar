package com.esa.moviestar.home;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.components.PopupMenu;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;



public class HeaderController {
    @FXML
    HBox navContainer;

    //Buttons
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
    @FXML
    private Node currentActive;
    @FXML
    private StackPane profileImage;
    @FXML
    private PopupMenu popupMenu;
    @FXML
    public void initialize() {
        currentActive = homeButton;
        currentActive.getStyleClass().remove("surface-transparent");
        currentActive.getStyleClass().add("primary");
        tbxSearch.setPromptText("Titles, Actors, Genres");
        profileImage.setOnMouseClicked(e -> popupMenu.show(profileImage));
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

    public void setUpPopUpMenu(MainPagesController mainPagesController, User user, Account account){
        UserDao userDao = new UserDao();
        List<User> users = userDao.findAllUsers(user.getEmail());
        setupPopupMenu(mainPagesController,account,user,users);
    }
    public TextField getTbxSearch(){
        return tbxSearch;
    }

    /**
     * Sets the visual style for the active button and resets the previous one.
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
        currentActive = button;
        if (currentActive != null) {
            currentActive.getStyleClass().remove("surface-transparent");
            if (!currentActive.getStyleClass().contains("primary")) {
                currentActive.getStyleClass().add("primary");
            }
            if (currentActive == searchButton) {
                tbxSearch.requestFocus();
            }
        }
    }


    private void setupPopupMenu(MainPagesController mainPagesController, Account account, User user, List<User> users) {
        // Create the popup menu - no stage needed
        popupMenu = new PopupMenu();

        // Add menu items
        HBox settingsItem = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().addAll("small-item", "chips", "surface-transparent");
        }};
        SVGPath profileIcon = new SVGPath() {{
            setContent(Main.resourceBundle.getString("user"));
            getStyleClass().add("on-primary");
        }};
        Text text = new Text("My Account") {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        settingsItem.getChildren().addAll(profileIcon, text);
        settingsItem.setOnMouseClicked(e -> {
                    mainPagesController.settingsClick(user,account);
                    popupMenu.close();
                }
        );

        popupMenu.addItem(settingsItem);
        popupMenu.addSeparator();
        for (User i : users) {
            if (user.getID()!=i.getID()) {
                createProfileItem(i, i.getName(), i.getIcon(), mainPagesController);
            }
        }
        if(users.size()<4){createAddItem(account,mainPagesController);}
        popupMenu.addSeparator();
        HBox emailButton = new HBox() {{
            setMinHeight(40.0);
            setAlignment(Pos.CENTER);
            getStyleClass().addAll("small-item", "surface-dim-border", "chips", "surface-transparent");
        }};
        SVGPath logoutIcon = new SVGPath() {{
            setContent(Main.resourceBundle.getString("logout"));
            getStyleClass().add("on-primary");
        }};
        Text logoutText = new Text("Logout") {{
            getStyleClass().addAll("medium-text", "on-primary");
        }};
        emailButton.setOnMouseClicked(e -> {mainPagesController.emailClick();popupMenu.close();});
        emailButton.getChildren().addAll(logoutIcon, logoutText);
        popupMenu.addItem(emailButton);

    }

    private void createProfileItem(User user, String name, Group profileIcon, MainPagesController mainPagesController) {
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
        item.setOnMouseClicked(e -> {mainPagesController.profileClick(user);popupMenu.close();});
        popupMenu.addItem(item);

    }



    public void setProfileIcon(Group icon) {
        profileImage.getChildren().clear();
        icon.setScaleX(2.286);
        icon.setScaleY(2.286);
        profileImage.getChildren().add(icon);
    }

    private void createAddItem(Account account , MainPagesController mainPagesController) {
        SVGPath cross = new SVGPath(){{
            setContent(Main.resourceBundle.getString("plusButton"));
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
        item.setOnMouseClicked(e -> {mainPagesController.createProfileUser(account);popupMenu.close();});
        popupMenu.addItem(item);

    }



}