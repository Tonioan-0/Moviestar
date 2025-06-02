package com.esa.moviestar.model;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.profile.IconSVG;
import javafx.scene.Group;

import java.time.LocalDate;
import  java.util.List;
import java.util.Vector;

public class User {

    private int codUser;         // PRIMARY KEY
    private String name;
    private String tastes;
    private int idImage;        // FOREIGN KEY verso tabella immagine
    private String email;          // FOREIGN KEY verso tabella Account
    private List<Integer> history;
    private LocalDate registrationDate;

    // Costruttore completo (es. DB)
    public User(int codUser, String name, String tastes, int idImage, String email, LocalDate registrationDate) {
        this.codUser = codUser;
        this.name = name;
        this.tastes = tastes;
        this.idImage = idImage;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // Costruttore senza codUtente (es.prima di inserimento nel DB)
    public User(String name, int idImage, String tastes, String email, LocalDate registrationDate) {
        this.name = name;
        this.idImage = idImage;
        this.tastes = tastes;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // Getter
    public int getID() { return codUser; }
    public String getName() { return name; }
    public String getTastes() { return tastes; }
    public Group getIcona() { return IconSVG.takeElement(idImage); }
    public int getIDIcona() { return idImage; }
    public String getEmail() { return email; }
    public List<Integer> getHistory(){
        return history;
    }
    public LocalDate getRegistrationDate(){return registrationDate;}

    // Setter
    public void setID(int codUtente) { this.codUser = codUtente; }
    public void setName(String name) { this.name = name; }
    public void setTastes(String tastes) { this.tastes = tastes; }
    public void setIcona(int idImmagine) { this.idImage = idImmagine; }
    public void setEmail(String email) { this.email = email; }
    public void setHistory(List<Integer> history) { this.history = history; }
    public void setRegistrationDate(LocalDate registrationDate){this.registrationDate = registrationDate;}

    public void verificaNomeValido() {
        if (name == null || name.isEmpty()) {
            System.out.println("Nome vuoto o nullo");
            return;
        }
        boolean invalid = false;
        char[] caratteri = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'
        };
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            for (char carattereVietato : caratteri) {
                if (c == carattereVietato) {
                    invalid = true;
                    break;
                }
            }
            if (invalid) break;
        }
        if (invalid) {
            System.out.println("Il nome ha un carattere non valido");
        } else {
            System.out.println("Il nome ha tutti caratteri ammessi");
        }
    }

    public List<Integer> getGustiComeLista() {
        List<Integer> pesi = new Vector<>();
        for (int i = 0; i < getTastes().length(); i += 2) {
            if (i + 2 <= getTastes().length()) {
                try {
                    pesi.add(Integer.parseInt(getTastes().substring(i, i + 2), 16));
                } catch (NumberFormatException e) {
                    System.err.println("User: i gusti potrebbero non essere corretti");
                }
            }
        }
        return pesi;
    }
    public List<Content> getCronologia(int limit) {
        ContentDao contentDao = new ContentDao();
        return contentDao.getWatched(this.codUser,limit);
    }

    public List<Content> getPreferiti(int limit) {
        ContentDao accountDao = new ContentDao();
        return accountDao.getFavourites(this.codUser,limit);
    }

    public List<Comment> getCommenti(int limit) {
//        UserDao utenteDao = new UserDao();
//        return utenteDao.getCommentiUtente(this.codUtente,limit);
        return null;
    }
    public int getUltimoGuardato() {
        return 5;
    }
}
