package com.gradebook.model;

import java.time.LocalDate;

public class DonneesPresence {
    private String matriculeEtudiant;
    private LocalDate dateAbsence;
    private String statut;
    private String matiere;
    private String sourceImport;

    public DonneesPresence() {
    }

    public DonneesPresence(String matriculeEtudiant, LocalDate dateAbsence, String statut, String matiere, String sourceImport) {
        this.matriculeEtudiant = matriculeEtudiant;
        this.dateAbsence = dateAbsence;
        this.statut = statut;
        this.matiere = matiere;
        this.sourceImport = sourceImport;
    }

    public String getMatriculeEtudiant() {
        return matriculeEtudiant;
    }

    public void setMatriculeEtudiant(String matriculeEtudiant) {
        this.matriculeEtudiant = matriculeEtudiant;
    }

    public LocalDate getDateAbsence() {
        return dateAbsence;
    }

    public void setDateAbsence(LocalDate dateAbsence) {
        this.dateAbsence = dateAbsence;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getSourceImport() {
        return sourceImport;
    }

    public void setSourceImport(String sourceImport) {
        this.sourceImport = sourceImport;
    }

    @Override
    public String toString() {
        return "DonneesPresence{" +
                "matriculeEtudiant='" + matriculeEtudiant + '\'' +
                ", dateAbsence=" + dateAbsence +
                ", statut='" + statut + '\'' +
                ", matiere='" + matiere + '\'' +
                ", sourceImport='" + sourceImport + '\'' +
                '}';
    }
}
