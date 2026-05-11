package com.gradebook.service;

import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Matiere;

import java.util.List;
import java.util.Optional;

public interface IReferentielService {
    Classe creerClasse(String nom, String niveau, String anneeAcademique);

    void modifierClasse(Classe classe);

    void supprimerClasse(int idClasse);

    List<Classe> getAllClasses();

    Optional<Classe> getClasseById(int idClasse);

    Matiere creerMatiere(String intitule, double coefficient);

    void modifierMatiere(Matiere matiere);

    void supprimerMatiere(int idMatiere);

    List<Matiere> getAllMatieres();

    Optional<Matiere> getMatiereById(int idMatiere);

    void affecterEnseignant(int idEnseignant, int idClasse, int idMatiere, int semestre);

    void retirerEnseignant(int idEnseignant, int idClasse, int idMatiere, int semestre);

    List<Matiere> getMatieresByEnseignantAndClasse(int idEnseignant, int idClasse);

    List<Matiere> getMatieresByEnseignantAndClasseAndSemestre(int idEnseignant, int idClasse, int semestre);

    void ajouterMatiereAClasse(int idClasse, int idMatiere);

    void retirerMatiereDeClasse(int idClasse, int idMatiere);

    List<Matiere> getMatieresByClasse(int idClasse);

    List<Matiere> getMatieresByClasseAndSemestre(int idClasse, int semestre);

    List<Classe> getClassesByMatiere(int idMatiere);

    boolean matiereExisteDansClasse(int idClasse, int idMatiere);

    List<Classe> getClassesByEnseignant(int idEnseignant);

    List<Classe> getClassesByEnseignantAndSemestre(int idEnseignant, int semestre);

    List<Integer> getSemestresByEnseignantAndClasse(int idEnseignant, int idClasse);

    List<Enseignant> getAllEnseignants();

    void ajouterEnseignant(Enseignant enseignant);

    void modifierEnseignant(Enseignant enseignant);

    void supprimerEnseignant(int idEnseignant);

    List<Enseignant> getEnseignantsByClasse(int idClasse);

    List<Etudiant> getAllEtudiants();

    List<Etudiant> getEtudiantsByClasse(int idClasse);

    void ajouterEtudiant(Etudiant etudiant);

    void modifierEtudiant(Etudiant etudiant);

    void supprimerEtudiant(int idEtudiant);
}
