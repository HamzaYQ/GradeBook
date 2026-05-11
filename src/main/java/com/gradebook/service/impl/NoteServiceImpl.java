package com.gradebook.service.impl;

import com.gradebook.dao.ICoursDao;
import com.gradebook.dao.IEvaluationDao;
import com.gradebook.dao.INoteDao;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Note;
import com.gradebook.service.INoteService;

import java.util.List;
import java.util.Optional;

public class NoteServiceImpl implements INoteService {
    private static final double NOTE_MIN = 0.0;
    private static final double NOTE_MAX = 20.0;

    private static final String ERR_NOTE_RANGE = "La note doit être entre 0 et 20";
    private static final String ERR_NOTE_EXISTE = "Une note existe déjà pour cet étudiant et cette évaluation";
    private static final String ERR_NOTE_INTROUVABLE = "Aucune note trouvée pour cet étudiant et cette évaluation";
    private static final String ERR_ENSEIGNANT_NON_AUTORISE = "Enseignant non autorisé pour cette matière";
    private static final String ERR_EVALUATION_INTROUVABLE = "Evaluation introuvable";

    private final INoteDao noteDao;
    private final IEvaluationDao evaluationDao;
    private final ICoursDao coursDao;

    public NoteServiceImpl(INoteDao noteDao, IEvaluationDao evaluationDao, ICoursDao coursDao) {
        this.noteDao = noteDao;
        this.evaluationDao = evaluationDao;
        this.coursDao = coursDao;
    }

    @Override
    public void saisirNote(int idEtudiant, int idEvaluation, int idEnseignant, double valeur) {
        validateValeur(valeur);

        Optional<Evaluation> evaluationOpt = evaluationDao.findById(idEvaluation);
        if (evaluationOpt.isEmpty()) {
            throw new IllegalStateException(ERR_EVALUATION_INTROUVABLE);
        }

        Evaluation evaluation = evaluationOpt.get();
        if (evaluation.getClasse() == null || evaluation.getMatiere() == null) {
            throw new IllegalStateException(ERR_EVALUATION_INTROUVABLE);
        }

        boolean autorise = coursDao.existsCours(
            idEnseignant,
            evaluation.getClasse().getId(),
            evaluation.getMatiere().getId(),
            evaluation.getSemestre()
        );
        if (!autorise) {
            throw new IllegalStateException(ERR_ENSEIGNANT_NON_AUTORISE);
        }

        if (noteExiste(idEtudiant, idEvaluation)) {
            throw new IllegalStateException(ERR_NOTE_EXISTE);
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setId(idEtudiant);

        Enseignant enseignant = new Enseignant();
        enseignant.setId(idEnseignant);

        Note note = new Note(etudiant, evaluation, valeur, enseignant);
        noteDao.save(note);
    }

    @Override
    public void modifierNote(int idEtudiant, int idEvaluation, int idEnseignant, double nouvelleValeur) {
        validateValeur(nouvelleValeur);

        if (!noteExiste(idEtudiant, idEvaluation)) {
            throw new IllegalStateException(ERR_NOTE_INTROUVABLE);
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setId(idEtudiant);

        Evaluation evaluation = new Evaluation();
        evaluation.setId(idEvaluation);

        Enseignant enseignant = new Enseignant();
        enseignant.setId(idEnseignant);

        Note note = new Note();
        note.setEtudiant(etudiant);
        note.setEvaluation(evaluation);
        note.setSaisiPar(enseignant);
        note.setValeur(nouvelleValeur);

        noteDao.update(note);
    }

    @Override
    public List<Note> getNotesByEvaluation(int idEvaluation) {
        return noteDao.findByEvaluation(idEvaluation);
    }

    @Override
    public List<Note> getNotesByEtudiant(int idEtudiant) {
        return noteDao.findByEtudiant(idEtudiant);
    }

    @Override
    public List<Note> getNotesByEtudiantAndMatiere(int idEtudiant, int idMatiere) {
        return noteDao.findByEtudiantAndMatiere(idEtudiant, idMatiere);
    }

    @Override
    public List<Note> getNotesByEtudiantAndSemestre(int idEtudiant, int semestre) {
        return noteDao.findByEtudiantAndSemestre(idEtudiant, semestre);
    }

    @Override
    public List<Note> getNotesByEtudiantAndMatiereAndSemestre(int idEtudiant, int idMatiere, int semestre) {
        return noteDao.findByEtudiantAndMatiereAndSemestre(idEtudiant, idMatiere, semestre);
    }

    @Override
    public boolean noteExiste(int idEtudiant, int idEvaluation) {
        return noteDao.existsByEtudiantAndEvaluation(idEtudiant, idEvaluation);
    }

    private void validateValeur(double valeur) {
        if (valeur < NOTE_MIN || valeur > NOTE_MAX) {
            throw new IllegalArgumentException(ERR_NOTE_RANGE);
        }
    }
}
