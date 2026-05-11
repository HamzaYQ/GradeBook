package com.gradebook.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReleveDeNotes {
    private int id;
    private int semestre;
    private String anneeAcademique;
    private double moyenneGenerale;
    private LocalDateTime genereLe;
    private Etudiant etudiant;
    private Administration genereParAdmin;
    private List<LigneReleve> lignes;

    public ReleveDeNotes() {
        this.lignes = new ArrayList<>();
    }

    public ReleveDeNotes(int id, int semestre, String anneeAcademique, Etudiant etudiant, Administration genereParAdmin) {
        this.id = id;
        setSemestre(semestre);
        this.anneeAcademique = anneeAcademique;
        this.etudiant = etudiant;
        this.genereParAdmin = genereParAdmin;
        this.genereLe = LocalDateTime.now();
        this.lignes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAnneeAcademique() {
        return anneeAcademique;
    }

    public void setAnneeAcademique(String anneeAcademique) {
        this.anneeAcademique = anneeAcademique;
    }

    public double getMoyenneGenerale() {
        return moyenneGenerale;
    }

    public void setMoyenneGenerale(double moyenneGenerale) {
        this.moyenneGenerale = moyenneGenerale;
    }

    public LocalDateTime getGenereLe() {
        return genereLe;
    }

    public void setGenereLe(LocalDateTime genereLe) {
        this.genereLe = genereLe;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public Administration getGenereParAdmin() {
        return genereParAdmin;
    }

    public void setGenereParAdmin(Administration genereParAdmin) {
        this.genereParAdmin = genereParAdmin;
    }

    public List<LigneReleve> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReleve> lignes) {
        this.lignes = lignes;
    }

    public void addLigne(LigneReleve ligne) {
        if (ligne == null) {
            return;
        }
        if (lignes == null) {
            lignes = new ArrayList<>();
        }
        lignes.add(ligne);
    }

    @Override
    public String toString() {
        Integer etudiantId = etudiant != null ? etudiant.getId() : null;
        Integer adminId = genereParAdmin != null ? genereParAdmin.getId() : null;
        int ligneCount = lignes != null ? lignes.size() : 0;
        return "ReleveDeNotes{" +
                "id=" + id +
            ", semestre=" + semestre +
                ", anneeAcademique='" + anneeAcademique + '\'' +
                ", moyenneGenerale=" + moyenneGenerale +
                ", genereLe=" + genereLe +
                ", etudiantId=" + etudiantId +
                ", genereParAdminId=" + adminId +
                ", lignes=" + ligneCount +
                '}';
    }
}
