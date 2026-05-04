package com.gradebook.model;

public enum Session {
    NORMALE("Normale"),
    RATTRAPAGE("Rattrapage");

    private final String libelle;

    Session(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
