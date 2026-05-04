package com.gradebook.model;

public enum Periode {
    SEMESTRE_1("Semestre 1"),
    SEMESTRE_2("Semestre 2"),
    ANNUEL("Annuel");

    private final String libelle;

    Periode(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
