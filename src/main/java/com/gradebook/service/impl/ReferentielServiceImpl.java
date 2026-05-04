package com.gradebook.service.impl;

import com.gradebook.dao.ICoursDao;
import com.gradebook.dao.IClasseDao;
import com.gradebook.dao.IEnseignantDao;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Matiere;
import com.gradebook.service.IReferentielService;

import java.util.List;
import java.util.Optional;

public class ReferentielServiceImpl implements IReferentielService {
    private static final String ERR_NOM_CLASSE = "Le nom de la classe est obligatoire";
    private static final String ERR_NIVEAU_CLASSE = "Le niveau est obligatoire";
    private static final String ERR_INTITULE_MATIERE = "L'intitule est obligatoire";
    private static final String ERR_COEFFICIENT_MATIERE = "Le coefficient doit être supérieur à 0";
    private static final String ERR_AFFECTATION_EXISTE = "Cette affectation existe déjà";

    private final IClasseDao classeDao;
    private final IMatiereDao matiereDao;
    private final IEnseignantDao enseignantDao;
    private final IEtudiantDao etudiantDao;
    private final ICoursDao coursDao;

    public ReferentielServiceImpl(IClasseDao classeDao, IMatiereDao matiereDao, IEnseignantDao enseignantDao,
                                  IEtudiantDao etudiantDao, ICoursDao coursDao) {
        this.classeDao = classeDao;
        this.matiereDao = matiereDao;
        this.enseignantDao = enseignantDao;
        this.etudiantDao = etudiantDao;
        this.coursDao = coursDao;
    }

    @Override
    public Classe creerClasse(String nom, String niveau, String anneeAcademique) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(ERR_NOM_CLASSE);
        }
        if (niveau == null || niveau.isBlank()) {
            throw new IllegalArgumentException(ERR_NIVEAU_CLASSE);
        }

        Classe classe = new Classe();
        classe.setNom(nom);
        classe.setNiveau(niveau);
        classe.setAnneeAcademique(anneeAcademique);

        classeDao.create(classe);
        return classe;
    }

    @Override
    public void modifierClasse(Classe classe) {
        classeDao.update(classe);
    }

    @Override
    public void supprimerClasse(int idClasse) {
        classeDao.delete(idClasse);
    }

    @Override
    public List<Classe> getAllClasses() {
        return classeDao.findAll();
    }

    @Override
    public Optional<Classe> getClasseById(int idClasse) {
        return classeDao.findById(idClasse);
    }

    @Override
    public Matiere creerMatiere(String intitule, double coefficient) {
        if (intitule == null || intitule.isBlank()) {
            throw new IllegalArgumentException(ERR_INTITULE_MATIERE);
        }
        if (coefficient <= 0.0) {
            throw new IllegalArgumentException(ERR_COEFFICIENT_MATIERE);
        }

        Matiere matiere = new Matiere();
        matiere.setIntitule(intitule);
        matiere.setCoefficient(coefficient);

        matiereDao.create(matiere);
        return matiere;
    }

    @Override
    public void modifierMatiere(Matiere matiere) {
        matiereDao.update(matiere);
    }

    @Override
    public void supprimerMatiere(int idMatiere) {
        matiereDao.delete(idMatiere);
    }

    @Override
    public List<Matiere> getAllMatieres() {
        return matiereDao.findAll();
    }

    @Override
    public Optional<Matiere> getMatiereById(int idMatiere) {
        return matiereDao.findById(idMatiere);
    }

    @Override
    public void affecterEnseignant(int idEnseignant, int idClasse, int idMatiere) {
        if (coursDao.existsCours(idEnseignant, idClasse, idMatiere)) {
            throw new IllegalStateException(ERR_AFFECTATION_EXISTE);
        }
        coursDao.addCours(idEnseignant, idClasse, idMatiere);
    }

    @Override
    public void retirerEnseignant(int idEnseignant, int idClasse, int idMatiere) {
        coursDao.removeCours(idEnseignant, idClasse, idMatiere);
    }

    @Override
    public List<Matiere> getMatieresByEnseignantAndClasse(int idEnseignant, int idClasse) {
        return coursDao.findMatieresByEnseignantAndClasse(idEnseignant, idClasse);
    }

    @Override
    public List<Classe> getClassesByEnseignant(int idEnseignant) {
        return coursDao.findClassesByEnseignant(idEnseignant);
    }

    @Override
    public List<Enseignant> getAllEnseignants() {
        return enseignantDao.findAll();
    }

    @Override
    public List<Etudiant> getAllEtudiants() {
        return etudiantDao.findAll();
    }

    @Override
    public List<Etudiant> getEtudiantsByClasse(int idClasse) {
        return etudiantDao.findByClasse(idClasse);
    }
}
