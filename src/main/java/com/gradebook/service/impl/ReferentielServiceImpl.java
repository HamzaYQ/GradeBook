package com.gradebook.service.impl;

import com.gradebook.dao.IClasseDao;
import com.gradebook.dao.IClasseMatiereDao;
import com.gradebook.dao.ICoursDao;
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
    private static final String ERR_AFFECTATION_EXISTE = "Cette affectation existe déjà pour ce semestre";
    private static final String ERR_CLASSE_INTROUVABLE = "Classe introuvable";
    private static final String ERR_MATIERE_INTROUVABLE = "Matière introuvable";
    private static final String ERR_CLASSE_MATIERE_EXISTE = "Cette matière est déjà associée à cette classe";
    private static final String ERR_MATIERE_NON_ASSOCIEE = "Cette matière n'est pas associée à cette classe";
    private static final String ERR_CLASSE_MATIERE_RETIRE =
            "Impossible de retirer : un enseignant est affecté à cette matière pour cette classe";
        private static final String ERR_MATIERE_RATTACHEE =
            "Impossible de supprimer : cette matière est rattachée à une classe";
        private static final String ERR_CNE_EXISTE = "Ce CNE existe déjà";
    private static final String ERR_EMAIL_EXISTE = "Cet email existe déjà";

    private final IClasseDao classeDao;
    private final IMatiereDao matiereDao;
    private final IEnseignantDao enseignantDao;
    private final IEtudiantDao etudiantDao;
    private final ICoursDao coursDao;
    private final IClasseMatiereDao classeMatiereDao;

    public ReferentielServiceImpl(IClasseDao classeDao, IMatiereDao matiereDao, IEnseignantDao enseignantDao,
                                  IEtudiantDao etudiantDao, ICoursDao coursDao, IClasseMatiereDao classeMatiereDao) {
        this.classeDao = classeDao;
        this.matiereDao = matiereDao;
        this.enseignantDao = enseignantDao;
        this.etudiantDao = etudiantDao;
        this.coursDao = coursDao;
        this.classeMatiereDao = classeMatiereDao;
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
        if (!classeMatiereDao.findClassesByMatiere(idMatiere).isEmpty()) {
            throw new IllegalStateException(ERR_MATIERE_RATTACHEE);
        }
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
    public void affecterEnseignant(int idEnseignant, int idClasse, int idMatiere, int semestre) {
        if (coursDao.existsCours(idEnseignant, idClasse, idMatiere, semestre)) {
            throw new IllegalStateException(ERR_AFFECTATION_EXISTE);
        }
        if (!classeMatiereDao.existsClasseMatiere(idClasse, idMatiere)) {
            throw new IllegalStateException(ERR_MATIERE_NON_ASSOCIEE);
        }
        coursDao.addCours(idEnseignant, idClasse, idMatiere, semestre);
    }

    @Override
    public void retirerEnseignant(int idEnseignant, int idClasse, int idMatiere, int semestre) {
        coursDao.removeCours(idEnseignant, idClasse, idMatiere, semestre);
    }

    @Override
    public List<Matiere> getMatieresByEnseignantAndClasse(int idEnseignant, int idClasse) {
        return coursDao.findMatieresByEnseignantAndClasse(idEnseignant, idClasse);
    }

    @Override
    public List<Matiere> getMatieresByEnseignantAndClasseAndSemestre(int idEnseignant, int idClasse, int semestre) {
        return coursDao.findMatieresByEnseignantAndClasseAndSemestre(idEnseignant, idClasse, semestre);
    }

    @Override
    public void ajouterMatiereAClasse(int idClasse, int idMatiere) {
        if (classeDao.findById(idClasse).isEmpty()) {
            throw new IllegalStateException(ERR_CLASSE_INTROUVABLE);
        }
        if (matiereDao.findById(idMatiere).isEmpty()) {
            throw new IllegalStateException(ERR_MATIERE_INTROUVABLE);
        }
        if (classeMatiereDao.existsClasseMatiere(idClasse, idMatiere)) {
            throw new IllegalStateException(ERR_CLASSE_MATIERE_EXISTE);
        }
        classeMatiereDao.addMatiere(idClasse, idMatiere);
    }

    @Override
    public void retirerMatiereDeClasse(int idClasse, int idMatiere) {
        if (!coursDao.findEnseignantsByClasseAndMatiere(idClasse, idMatiere).isEmpty()) {
            throw new IllegalStateException(ERR_CLASSE_MATIERE_RETIRE);
        }
        classeMatiereDao.removeMatiere(idClasse, idMatiere);
    }

    @Override
    public List<Matiere> getMatieresByClasse(int idClasse) {
        return classeMatiereDao.findMatieresByClasse(idClasse);
    }

    @Override
    public List<Matiere> getMatieresByClasseAndSemestre(int idClasse, int semestre) {
        return classeMatiereDao.findMatieresByClasse(idClasse);
    }

    @Override
    public List<Classe> getClassesByMatiere(int idMatiere) {
        return classeMatiereDao.findClassesByMatiere(idMatiere);
    }

    @Override
    public boolean matiereExisteDansClasse(int idClasse, int idMatiere) {
        return classeMatiereDao.existsClasseMatiere(idClasse, idMatiere);
    }

    @Override
    public List<Classe> getClassesByEnseignant(int idEnseignant) {
        return coursDao.findClassesByEnseignant(idEnseignant);
    }

    @Override
    public List<Classe> getClassesByEnseignantAndSemestre(int idEnseignant, int semestre) {
        return coursDao.findClassesByEnseignantAndSemestre(idEnseignant, semestre);
    }

    @Override
    public List<Integer> getSemestresByEnseignantAndClasse(int idEnseignant, int idClasse) {
        return coursDao.findSemestresByEnseignantAndClasse(idEnseignant, idClasse);
    }

    @Override
    public List<Enseignant> getAllEnseignants() {
        return enseignantDao.findAll();
    }

    @Override
    public void ajouterEnseignant(Enseignant enseignant) {
        if (enseignantDao.findByEmail(enseignant.getEmail()).isPresent()) {
            throw new IllegalStateException(ERR_EMAIL_EXISTE);
        }
        enseignantDao.create(enseignant);
    }

    @Override
    public void modifierEnseignant(Enseignant enseignant) {
        enseignantDao.update(enseignant);
    }

    @Override
    public void supprimerEnseignant(int idEnseignant) {
        enseignantDao.delete(idEnseignant);
    }

    @Override
    public List<Enseignant> getEnseignantsByClasse(int idClasse) {
        return coursDao.findEnseignantsByClasse(idClasse);
    }

    @Override
    public List<Etudiant> getAllEtudiants() {
        return etudiantDao.findAll();
    }

    @Override
    public List<Etudiant> getEtudiantsByClasse(int idClasse) {
        return etudiantDao.findByClasse(idClasse);
    }

    @Override
    public void ajouterEtudiant(Etudiant etudiant) {
        if (etudiantDao.findByCne(etudiant.getCne()).isPresent()) {
            throw new IllegalStateException(ERR_CNE_EXISTE);
        }
        if (etudiantDao.findByEmail(etudiant.getEmail()).isPresent()) {
            throw new IllegalStateException(ERR_EMAIL_EXISTE);
        }
        etudiantDao.create(etudiant);
    }

    @Override
    public void modifierEtudiant(Etudiant etudiant) {
        etudiantDao.update(etudiant);
    }

    @Override
    public void supprimerEtudiant(int idEtudiant) {
        etudiantDao.delete(idEtudiant);
    }
}
