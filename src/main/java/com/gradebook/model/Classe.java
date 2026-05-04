package com.gradebook.model;

import java.util.ArrayList;
import java.util.List;

public class Classe {
    private int id;
    private String nom;
    private String niveau;
    private String anneeAcademique;
    private List<Etudiant> etudiants;
    private List<Evaluation> evaluations;

    public Classe() {
        this.etudiants = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    public Classe(int id, String nom, String niveau, String anneeAcademique) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.anneeAcademique = anneeAcademique;
        this.etudiants = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getAnneeAcademique() {
        return anneeAcademique;
    }

    public void setAnneeAcademique(String anneeAcademique) {
        this.anneeAcademique = anneeAcademique;
    }

    public List<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public void addEtudiant(Etudiant etudiant) {
        if (etudiant == null) {
            return;
        }
        if (etudiants == null) {
            etudiants = new ArrayList<>();
        }
        etudiants.add(etudiant);
    }

    public void removeEtudiant(Etudiant etudiant) {
        if (etudiants == null || etudiant == null) {
            return;
        }
        etudiants.remove(etudiant);
    }

    public void addEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            return;
        }
        if (evaluations == null) {
            evaluations = new ArrayList<>();
        }
        evaluations.add(evaluation);
    }

    @Override
    public String toString() {
        int etudiantCount = etudiants != null ? etudiants.size() : 0;
        int evaluationCount = evaluations != null ? evaluations.size() : 0;
        return "Classe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", niveau='" + niveau + '\'' +
                ", anneeAcademique='" + anneeAcademique + '\'' +
                ", etudiants=" + etudiantCount +
                ", evaluations=" + evaluationCount +
                '}';
    }
}
