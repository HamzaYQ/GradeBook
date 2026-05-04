package com.gradebook.model;

public class LigneReleve {
    private Matiere matiere;
    private double moyenneMatiere;
    private double coefficient;

    public LigneReleve() {
    }

    public LigneReleve(Matiere matiere, double moyenneMatiere, double coefficient) {
        this.matiere = matiere;
        this.moyenneMatiere = moyenneMatiere;
        this.coefficient = coefficient;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public double getMoyenneMatiere() {
        return moyenneMatiere;
    }

    public void setMoyenneMatiere(double moyenneMatiere) {
        this.moyenneMatiere = moyenneMatiere;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public String toString() {
        String matiereIntitule = matiere != null ? matiere.getIntitule() : null;
        return "LigneReleve{" +
                "matiere='" + matiereIntitule + '\'' +
                ", moyenneMatiere=" + moyenneMatiere +
                ", coefficient=" + coefficient +
                '}';
    }
}
