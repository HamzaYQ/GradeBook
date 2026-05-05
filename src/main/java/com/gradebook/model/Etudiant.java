package com.gradebook.model;

public class Etudiant extends Utilisateur {
    private String cne;
    private Classe classe;

    public Etudiant() {
    }

    public Etudiant(int id, String nom, String prenom, String email, String motDePasse, String cne, Classe classe) {
        super(id, nom, prenom, email, motDePasse);
        this.cne = cne;
        this.classe = classe;
    }

    public String getCne() {
        return cne;
    }

    public void setCne(String cne) {
        this.cne = cne;
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
            ", cne='" + cne + '\'' +
                ", classeId=" + classeId +
                ", classeNom='" + classeNom + '\'' +
                '}';
    }
}
