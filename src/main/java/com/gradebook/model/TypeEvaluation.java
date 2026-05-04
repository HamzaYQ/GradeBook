package com.gradebook.model;

public enum TypeEvaluation {
    EXAMEN_FINAL("Examen Final"),
    CONTROLE_CONTINU("Controle Continu"),
    TP("TP"),
    PROJET("Projet");

    private final String libelle;

    TypeEvaluation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
