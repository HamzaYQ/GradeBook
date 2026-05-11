package com.gradebook.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    private int id;
    private String libelle;
    private TypeEvaluation type;
    private Session session;
    private int semestre;
    private double coefficient;
    private LocalDate dateSession;
    private Matiere matiere;
    private Classe classe;
    private Enseignant enseignant;
    private List<Note> notes;

    public Evaluation() {
        this.notes = new ArrayList<>();
    }

    public Evaluation(int id, String libelle, TypeEvaluation type, Session session, int semestre, double coefficient,
                      LocalDate dateSession, Matiere matiere, Classe classe, Enseignant enseignant) {
        this.id = id;
        this.libelle = libelle;
        this.type = type;
        this.session = session;
        setSemestre(semestre);
        this.dateSession = dateSession;
        this.matiere = matiere;
        this.classe = classe;
        this.enseignant = enseignant;
        this.notes = new ArrayList<>();
        setCoefficient(coefficient);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public TypeEvaluation getType() {
        return type;
    }

    public void setType(TypeEvaluation type) {
        this.type = type;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        if (semestre != 1 && semestre != 2) {
            throw new IllegalArgumentException("Le semestre doit être 1 ou 2");
        }
        this.semestre = semestre;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        if (coefficient <= 0) {
            throw new IllegalArgumentException("Le coefficient de l'évaluation doit être supérieur à 0");
        }
        this.coefficient = coefficient;
    }

    public LocalDate getDateSession() {
        return dateSession;
    }

    public void setDateSession(LocalDate dateSession) {
        this.dateSession = dateSession;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        Integer matiereId = matiere != null ? matiere.getId() : null;
        Integer classeId = classe != null ? classe.getId() : null;
        Integer enseignantId = enseignant != null ? enseignant.getId() : null;
        int noteCount = notes != null ? notes.size() : 0;
        return "Evaluation{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", type=" + type +
                ", session=" + session +
                ", semestre=" + semestre +
                ", coefficient=" + coefficient +
                ", dateSession=" + dateSession +
                ", matiereId=" + matiereId +
                ", classeId=" + classeId +
                ", enseignantId=" + enseignantId +
                ", notes=" + noteCount +
                '}';
    }
}
