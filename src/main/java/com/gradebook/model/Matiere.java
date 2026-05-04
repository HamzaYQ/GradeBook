package com.gradebook.model;

import java.util.ArrayList;
import java.util.List;

public class Matiere {
    private int id;
    private String intitule;
    private double coefficient;
    private List<Evaluation> evaluations;

    public Matiere() {
        this.evaluations = new ArrayList<>();
    }

    public Matiere(int id, String intitule, double coefficient) {
        this.id = id;
        this.intitule = intitule;
        this.evaluations = new ArrayList<>();
        setCoefficient(coefficient);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        if (coefficient <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0");
        }
        this.coefficient = coefficient;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
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
        int evaluationCount = evaluations != null ? evaluations.size() : 0;
        return "Matiere{" +
                "id=" + id +
                ", intitule='" + intitule + '\'' +
                ", coefficient=" + coefficient +
                ", evaluations=" + evaluationCount +
                '}';
    }
}
