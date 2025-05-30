package com.esa.moviestar.settings;

import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;

public class WatchListController {
    private Account account;
    private Utente utente;

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setUtente(Utente utente) {
        this.utente=utente;
    }
}
