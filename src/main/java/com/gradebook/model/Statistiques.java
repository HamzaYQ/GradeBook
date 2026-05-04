package com.gradebook.model;

public class Statistiques {
    private double moyenneClasse;
    private double noteMax;
    private double noteMin;
    private int nbEtudiants;

    public Statistiques() {
    }

    public Statistiques(double moyenneClasse, double noteMax, double noteMin, int nbEtudiants) {
        this.moyenneClasse = moyenneClasse;
        this.noteMax = noteMax;
        this.noteMin = noteMin;
        this.nbEtudiants = nbEtudiants;
    }

    public double getMoyenneClasse() {
        return moyenneClasse;
    }

    public void setMoyenneClasse(double moyenneClasse) {
        this.moyenneClasse = moyenneClasse;
    }

    public double getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(double noteMax) {
        this.noteMax = noteMax;
    }

    public double getNoteMin() {
        return noteMin;
    }

    public void setNoteMin(double noteMin) {
        this.noteMin = noteMin;
    }

    public int getNbEtudiants() {
        return nbEtudiants;
    }

    public void setNbEtudiants(int nbEtudiants) {
        this.nbEtudiants = nbEtudiants;
    }

    @Override
    public String toString() {
        return "Statistiques{" +
                "moyenneClasse=" + moyenneClasse +
                ", noteMax=" + noteMax +
                ", noteMin=" + noteMin +
                ", nbEtudiants=" + nbEtudiants +
                '}';
    }
}
