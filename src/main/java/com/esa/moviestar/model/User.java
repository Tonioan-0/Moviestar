package com.esa.moviestar.model;

import com.esa.moviestar.profile.IconSVG;
import javafx.scene.Group;
import java.time.LocalDate;

public class User {

    private int codUser;         // PRIMARY KEY
    private String name;
    private int idImage;
    private String email;          // FOREIGN KEY
    private LocalDate registrationDate;

    public User(int codUser, String name, int idImage, String email, LocalDate registrationDate) {
        this.codUser = codUser;
        this.name = name;
        this.idImage = idImage;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    public User(String name, int idImage, String email, LocalDate registrationDate) {
        this.name = name;
        this.idImage = idImage;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // Getter
    public int getID() { return codUser; }
    public String getName() { return name; }
    public Group getIcon() { return IconSVG.takeElement(idImage); }
    public int getIDIcon() { return idImage; }
    public String getEmail() { return email; }
    public LocalDate getRegistrationDate(){return registrationDate;}

    // Setter
    public void setID(int codUser) { this.codUser = codUser; }
    public void setName(String name) { this.name = name; }
    public void setIcon(int idImage) { this.idImage = idImage; }
    public void setEmail(String email) { this.email = email; }
    public void setRegistrationDate(LocalDate registrationDate){this.registrationDate = registrationDate;}

}
