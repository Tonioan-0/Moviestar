package com.esa.moviestar.model;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.profile.IconSVG;
import javafx.scene.Group;

import java.time.LocalDate;
import  java.util.List;
import java.util.Vector;

public class Utente {

    private int codUtente;         // PRIMARY KEY
    private String nome;
    private String gusti;
    private int idImmagine;        // FOREIGN KEY verso tabella immagine
    private String email;          // FOREIGN KEY verso tabella Account
    private List<Integer> cronologia;
    private LocalDate dataRegistrazione;

    // Costruttore completo (es. DB)
    public Utente(int codUtente, String nome, String gusti, int idImmagine, String email,LocalDate dataRegistrazione) {
        this.codUtente = codUtente;
        this.nome = nome;
        this.gusti = gusti;
        this.idImmagine = idImmagine;
        this.email = email;
        this.dataRegistrazione=dataRegistrazione;
    }

    // Costruttore senza codUtente (es.prima di inserimento nel DB)
    public Utente(String nome, int idImmagine, String gusti, String email,LocalDate dataRegistrazione) {
        this.nome = nome;
        this.idImmagine = idImmagine;
        this.gusti = gusti;
        this.email = email;
        this.dataRegistrazione=dataRegistrazione;
    }

    // Getter
    public int getID() { return codUtente; }
    public String getNome() { return nome; }
    public String getGusti() { return gusti; }
    public Group getIcona() { return IconSVG.takeElement(idImmagine); }
    public int getIDIcona() { return idImmagine; }
    public String getEmail() { return email; }
    public List<Integer> getCronologia(){
        return cronologia;
    }
    public LocalDate getDataRegistrazione(){return dataRegistrazione;}

    // Setter
    public void setID(int codUtente) { this.codUtente = codUtente; }
    public void setNome(String nome) { this.nome = nome; }
    public void setGusti(String gusti) { this.gusti = gusti; }
    public void setIcona(int idImmagine) { this.idImmagine = idImmagine; }
    public void setEmail(String email) { this.email = email; }
    public void setCronologia(List<Integer> cronologia) { this.cronologia = cronologia; }
    public void setDataRegistrazione(LocalDate dataRegistrazione){this.dataRegistrazione=dataRegistrazione;}

    // Verifica del nome (rimasta invariata)
    public void verificaNomeValido() {
        if (nome == null || nome.isEmpty()) {
            System.out.println("Nome vuoto o nullo");
            return;
        }
        boolean invalid = false;
        char[] caratteri = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'
        };
        for (int i = 0; i < nome.length(); i++) {
            char c = nome.charAt(i);
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
        for (int i = 0; i < getGusti().length(); i += 2) {
            if (i + 2 <= getGusti().length()) {
                try {
                    pesi.add(Integer.parseInt(getGusti().substring(i, i + 2), 16));
                } catch (NumberFormatException e) {
                    System.err.println("Utente: i gusti potrebbero non essere corretti");
                }
            }
        }
        return pesi;
    }
    public List<Content> getCronologia(int limit) {
        ContentDao contentDao = new ContentDao();
        return contentDao.getWatched(this.codUtente,limit);
    }

    public List<Content> getPreferiti(int limit) {
        ContentDao accountDao = new ContentDao();
        return accountDao.getFavourites(this.codUtente,limit);
    }

    public List<Comment> getCommenti(int limit) {
//        UtenteDao utenteDao = new UtenteDao();
//        return utenteDao.getCommentiUtente(this.codUtente,limit);
        return null;
    }
    public int getUltimoGuardato() {
        return 5;
    }
}
