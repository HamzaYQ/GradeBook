package com.gradebook.model;

import java.time.LocalDateTime;

public class Note {
    private Etudiant etudiant;
    private Evaluation evaluation;
    private double valeur;
    private Enseignant saisiPar;
    private LocalDateTime dateSaisie;
    private LocalDateTime updatedAt;

    public Note() {
    }

    public Note(Etudiant etudiant, Evaluation evaluation, double valeur, Enseignant saisiPar) {
        this.etudiant = etudiant;
        this.evaluation = evaluation;
        this.saisiPar = saisiPar;
        setValeur(valeur);
        this.dateSaisie = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        if (valeur < 0 || valeur > 20) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20");
        }
        this.valeur = valeur;
    }

    public Enseignant getSaisiPar() {
        return saisiPar;
    }

    public void setSaisiPar(Enseignant saisiPar) {
        this.saisiPar = saisiPar;
    }

    public LocalDateTime getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(LocalDateTime dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        Integer etudiantId = etudiant != null ? etudiant.getId() : null;
        Integer evaluationId = evaluation != null ? evaluation.getId() : null;
        Integer enseignantId = saisiPar != null ? saisiPar.getId() : null;
        return "Note{" +
                "etudiantId=" + etudiantId +
                ", evaluationId=" + evaluationId +
                ", valeur=" + valeur +
                ", saisiParId=" + enseignantId +
                ", dateSaisie=" + dateSaisie +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
