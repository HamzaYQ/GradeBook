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

    void affecterEnseignant(int idEnseignant, int idClasse, int idMatiere);

    void retirerEnseignant(int idEnseignant, int idClasse, int idMatiere);

    List<Matiere> getMatieresByEnseignantAndClasse(int idEnseignant, int idClasse);

    List<Classe> getClassesByEnseignant(int idEnseignant);

    List<Enseignant> getAllEnseignants();

    List<Etudiant> getAllEtudiants();

    List<Etudiant> getEtudiantsByClasse(int idClasse);
}
