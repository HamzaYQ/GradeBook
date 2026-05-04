package com.gradebook.model;

public class Etudiant extends Utilisateur {
    private String matricule;
    private Classe classe;

    public Etudiant() {
    }

    public Etudiant(int id, String nom, String prenom, String email, String motDePasse, String matricule, Classe classe) {
        super(id, nom, prenom, email, motDePasse);
        this.matricule = matricule;
        this.classe = classe;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    @Override
    public String toString() {
        Integer classeId = classe != null ? classe.getId() : null;
        String classeNom = classe != null ? classe.getNom() : null;
        return "Etudiant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", matricule='" + matricule + '\'' +
                ", classeId=" + classeId +
                ", classeNom='" + classeNom + '\'' +
                '}';
    }
}
