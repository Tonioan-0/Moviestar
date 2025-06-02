package com.esa.moviestar.model;

public class Account {


    private String email; //primary key
    private String password;
    private User[] user; //

    //Costruttore
    public Account(String email , String password){
        this.email=email;
        this.password=password;
        this.user = new User[4];
    }

    //metodi setter
    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    //metodi getter
    public String getEmail() {return email;}
    public String getPassword() {return password;}


    // Metodo per ottenere gli utenti
    public User[] getUser() {
        return user;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Account user = (Account) obj;
        return email.equals(user.email);
    }
    @Override
    public int hashCode() {
        return email.hashCode();
    }



}
