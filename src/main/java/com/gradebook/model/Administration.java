package com.gradebook.model;

public class Administration extends Utilisateur {
    public Administration() {
    }

    public Administration(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse);
    }

    @Override
    public String toString() {
        return "Administration{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
