package com.esa.moviestar.settings;

import com.esa.moviestar.model.Content;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.Objects;

public class ContentCardContainer {
    @FXML
    private Text contentDescription;
    @FXML
    private Label contentRuntime;
    @FXML
    private Label contentYear;
    @FXML
    private Label contentTitle;
    @FXML
    private ImageView imgView;

    public int idContent;

    public void setContentInformation(Content content){
        idContent=content.getId();
        contentTitle.setText(content.getTitle());
        contentDescription.setText(content.getPlot());
        contentYear.setText(String.valueOf(content.getYear()));

        try {
            if (content.getImageUrl() == null || content.getImageUrl().isEmpty() || Objects.equals(content.getImageUrl(), "error")) {
                imgView.setImage(null);
                return;
            }
            Image img = new Image(content.getImageUrl(), true);
            img.errorProperty().addListener((observable, oldValue, newValue) -> {
                Exception e = img.getException();
                if (e != null) {
                    System.err.println("Error loading image '" + content.getImageUrl());

                } else {
                    System.err.println("Error loading image '" + content.getImageUrl() + "': Unknown error.");
                }
            });
            img.progressProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0) {
                    imgView.setImage(img);

                }
            });

        } catch (Exception e) {
            System.err.println("FilmCardController: " + e.getMessage());
        }
    }

}
