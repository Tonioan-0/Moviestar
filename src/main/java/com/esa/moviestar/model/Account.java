package com.esa.moviestar.model;

public class Account {


    private String email; //primary key
    private String password;
    private Utente[] utenti; //

    //Costruttore
    public Account(String email , String password){
        this.email=email;
        this.password=password;
        this.utenti = new Utente[4];
    }

    //metodi setter
    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    //metodi getter
    public String getEmail() {return email;}
    public String getPassword() {return password;}

    //metodo per aggiungere un utente
    public boolean aggiungiUtente(Utente utente){
        for(int i = 0 ; i < utenti.length; i++){
            if(utenti[i]==null){
                utenti[i]=utente;
                return true;
            }
        }
        System.out.println("Non puoi aggiungere più di 4 utenti a questo account.");
        return false; // tutti i posti sono occupati
    }

    // Metodo per ottenere gli utenti
    public Utente[] getUtenti() {
        return utenti;
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

    /*public void verificaPassword() {
        // Requisiti password:
        // 1) lunghezza minima di 8 caratteri
        // 2) almeno una lettera maiuscola
        // 3) almeno un numero
        // 4) almeno un carattere speciale
        // 5) nessuno spazio

        boolean lunghezzacorretta;
        if (password.length() < 8) {
            lunghezzacorretta = false;
            System.out.println("Password troppo corta");
        } else {
            lunghezzacorretta = true;
            System.out.println("Password di lunghezza corretta");
        }

        boolean maiuscola = false;
        char[] lettereMaiuscole = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

        for (int i = 0; i < password.length(); i++) {
            for (int j = 0; j < lettereMaiuscole.length; j++) {
                if (password.charAt(i) == lettereMaiuscole[j]) {
                    maiuscola = true;
                    break;
                }
            }
        }

        if (maiuscola) {
            System.out.println("È presente una lettera maiuscola");
        } else {
            System.out.println("Non è presente una lettera maiuscola");
        }


        boolean hanumero = false;
        char[] numeri = {'0','1','2','3','4','5','6','7','8','9'};

        for (int i = 0; i < password.length(); i++) {
            for (int j = 0; j < numeri.length; j++) {
                if (password.charAt(i) == numeri[j]) {
                    hanumero = true;
                    break;
                }
            }
        }

        if (hanumero) {
            System.out.println("È presente un numero");
        } else {
            System.out.println("Non è presente un numero");
        }


        boolean caratterespeciale = false;
        char[] special = { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'};

        for (int i = 0; i < password.length(); i++) {
            for (int j = 0; j < special.length; j++) {
                if (password.charAt(i) == special[j]) {
                    caratterespeciale = true;
                    break;
                }
            }
        }

        if (caratterespeciale) {
            System.out.println("È presente un carattere speciale");
        } else {
            System.out.println("Non è presente un carattere speciale");
        }


        boolean haspazi = false;
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) == ' ') {
                haspazi = true;
                break;
            }
        }

        if (haspazi) {
            System.out.println("È presente uno spazio");
        } else {
            System.out.println("Non è presente uno spazio");
        }

        if (lunghezzacorretta && maiuscola && hanumero && caratterespeciale && !haspazi) {
            System.out.println(" Password valida!");
        } else {
            System.out.println(" Password non valida!");
        }

    }


    public void VerificaEmailValida(){
        //controllo che esista un @ unica all'interno dell'email
        int contatore1=0; //contatore delle @
        for (int i = 0 ; i < email.length(); i++) {
            if (email.charAt(i)=='@'){
                contatore1+=1;
            }
        }
        if (contatore1==1){
            System.out.println("email con una sola @");
        }else {
            System.out.println("email non valida , ci sono o troppe @ oppure zero @ ");
        }

        //Controllo che esista un unico punto (.) all'interno dell'email
        int contatore2=0; //contatore dei punti
        for(int i = 0 ;  i < email.length(); i++){
            if (email.charAt(i)=='.') {
                contatore2+=1;
            }
        }
        if (contatore2==1){
            System.out.println("email con un solo . ");
        }else {
            System.out.println("email non valida , ci sono o troppi . oppure zero . ");
        }

        //adesso l'idea è dividere la stringa email in 3 parti diverse nome , dominio , estensione

        //1 step , trovare l'indice a cui si trovano @ e .
        int posizioneChiocciola=-1; //la imposto a un indice impossibile
        int posizionePunto=-1;      //la imposto a un indice impossibile

        for(int i = 0 ; i < email.length() ; i++){
            if (email.charAt(i)=='.'){
                posizionePunto=i;
            }
            if (email.charAt(i)=='@'){
                posizioneChiocciola=i;
            }
        }

        //2 step riempire i 3 campi dei nomi , dominio ed estensione
        String nome="";
        String dominio="";
        String estensione="";
        for (int i = 0 ; i < email.length(); i++) {
            if (i<posizioneChiocciola) {
                nome+=email.charAt(i);
            }else if (i>posizioneChiocciola && i<posizionePunto) {
                dominio+=email.charAt(i);
            }else if (i>posizionePunto) {
                estensione+=email.charAt(i);
            }
        }

        //3 step fare i controlli sul nome sul dominio e sull'estensione
        //controllo sul nome
        if (nome.isEmpty()){
            System.out.println("nome non valido , quindi email non valida ");
        }

        //controllo sul dominio
        if (dominio.isEmpty()){
            System.out.println("dominio non valido , quindi email non valida ");  //controllo che il dominio non sia vuoto
        }
        String[] dominiValidi = {"gmail", "yahoo", "outlook", "hotmail"};  //controllo che il dominio che scrivo nell'email sia presente tra i domini validi
        boolean DomVal=false;
        for (int i = 0 ; i < dominiValidi.length; i++) {
            if(dominio.equals(dominiValidi[i])){
                DomVal=true;
                break;
            }
        }
        if (DomVal) {
            System.out.println("Dominio valido");
        }else{
            System.out.println("Dominio non valido");
        }

        //controllo sulle estensioni
        if (estensione.isEmpty()) {
            System.out.println("estensione non valida , quindi email non valida ");  //controllo che l'estensione non sia vuota
        }
        String[] estensioniValide = {"com", "it", "net", "org"};
        boolean EstVal=false;
        for(int i = 0 ; i < estensioniValide.length; i++){
            if(estensione.equals(estensioniValide[i])){
                EstVal=true;
                break;
            }
        }
        if (EstVal) {
            System.out.println("Estensione valida");
        }else{
            System.out.println("Estensione non valida");
        }

    }*/

}
