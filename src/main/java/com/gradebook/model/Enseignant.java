package com.gradebook.model;

import java.util.ArrayList;
import java.util.List;

public class Enseignant extends Utilisateur {
    private List<Matiere> matieres;
    private List<Classe> classes;

    public Enseignant() {
        this.matieres = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public Enseignant(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse);
        this.matieres = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public List<Matiere> getMatieres() {
        return matieres;
    }

    public void setMatieres(List<Matiere> matieres) {
        this.matieres = matieres;
    }

    public List<Classe> getClasses() {
        return classes;
    }

    public void setClasses(List<Classe> classes) {
        this.classes = classes;
    }

    public void addMatiere(Matiere matiere) {
        if (matiere == null) {
            return;
        }
        if (matieres == null) {
            matieres = new ArrayList<>();
        }
        matieres.add(matiere);
    }

    public void addClass(Classe classe) {
        if (classe == null) {
            return;
        }
        if (classes == null) {
            classes = new ArrayList<>();
        }
        classes.add(classe);
    }

    @Override
    public String toString() {
        int matiereCount = matieres != null ? matieres.size() : 0;
        int classeCount = classes != null ? classes.size() : 0;
        return "Enseignant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", matieres=" + matiereCount +
                ", classes=" + classeCount +
                '}';
    }
}
