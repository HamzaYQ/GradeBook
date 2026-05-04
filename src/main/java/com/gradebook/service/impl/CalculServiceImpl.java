package com.gradebook.service.impl;

import com.gradebook.dao.IEvaluationDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.dao.INoteDao;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Matiere;
import com.gradebook.model.Note;
import com.gradebook.model.Statistiques;
import com.gradebook.service.ICalculService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculServiceImpl implements ICalculService {
    private static final double ROUND_TWO = 100.0;
    private static final double NOTE_MIN_DEFAULT = 0.0;

    private static final double THRESHOLD_PASSABLE = 10.0;
    private static final double THRESHOLD_ASSEZ_BIEN = 12.0;
    private static final double THRESHOLD_BIEN = 14.0;
    private static final double THRESHOLD_TRES_BIEN = 16.0;

    private final INoteDao noteDao;
    private final IEvaluationDao evaluationDao;
    private final IMatiereDao matiereDao;

    public CalculServiceImpl(INoteDao noteDao, IEvaluationDao evaluationDao, IMatiereDao matiereDao) {
        this.noteDao = noteDao;
        this.evaluationDao = evaluationDao;
        this.matiereDao = matiereDao;
    }

    @Override
    public double calculerMoyenneParMatiere(int idEtudiant, int idMatiere) {
        List<Note> notes = noteDao.findByEtudiantAndMatiere(idEtudiant, idMatiere);
        if (notes.isEmpty()) {
            return NOTE_MIN_DEFAULT;
        }

        double sommePonderee = 0.0;
        double sommeCoeff = 0.0;

        for (Note note : notes) {
            double coeff = note.getEvaluation() != null ? note.getEvaluation().getCoefficient() : 0.0;
            sommePonderee += note.getValeur() * coeff;
            sommeCoeff += coeff;
        }

        if (sommeCoeff == 0.0) {
            return NOTE_MIN_DEFAULT;
        }

        return roundTwo(sommePonderee / sommeCoeff);
    }

    @Override
    public double calculerMoyenneGenerale(int idEtudiant, int idClasse) {
        List<Matiere> matieres = matiereDao.findByClasse(idClasse);
        if (matieres.isEmpty()) {
            return NOTE_MIN_DEFAULT;
        }

        double sommePonderee = 0.0;
        double sommeCoeff = 0.0;

        for (Matiere matiere : matieres) {
            double moyenne = calculerMoyenneParMatiere(idEtudiant, matiere.getId());
            double coeff = matiere.getCoefficient();
            sommePonderee += moyenne * coeff;
            sommeCoeff += coeff;
        }

        if (sommeCoeff == 0.0) {
            return NOTE_MIN_DEFAULT;
        }

        return roundTwo(sommePonderee / sommeCoeff);
    }

    @Override
    public Statistiques calculerStatistiquesEvaluation(int idEvaluation) {
        List<Note> notes = noteDao.findByEvaluation(idEvaluation);
        if (notes.isEmpty()) {
            return new Statistiques(0.0, 0.0, 0.0, 0);
        }

        double somme = 0.0;
        double noteMax = Double.NEGATIVE_INFINITY;
        double noteMin = Double.POSITIVE_INFINITY;

        for (Note note : notes) {
            double valeur = note.getValeur();
            somme += valeur;
            noteMax = Math.max(noteMax, valeur);
            noteMin = Math.min(noteMin, valeur);
        }

        double moyenne = roundTwo(somme / notes.size());
        return new Statistiques(moyenne, noteMax, noteMin, notes.size());
    }

    @Override
    public Statistiques calculerStatistiquesClasse(int idClasse, int idMatiere) {
        List<Evaluation> evaluations = evaluationDao.findByClasseAndMatiere(idClasse, idMatiere);
        if (evaluations.isEmpty()) {
            return new Statistiques(0.0, 0.0, 0.0, 0);
        }

        double somme = 0.0;
        double noteMax = Double.NEGATIVE_INFINITY;
        double noteMin = Double.POSITIVE_INFINITY;
        int totalNotes = 0;
        Set<Integer> etudiants = new HashSet<>();

        for (Evaluation evaluation : evaluations) {
            List<Note> notes = noteDao.findByEvaluation(evaluation.getId());
            for (Note note : notes) {
                double valeur = note.getValeur();
                somme += valeur;
                noteMax = Math.max(noteMax, valeur);
                noteMin = Math.min(noteMin, valeur);
                totalNotes++;
                if (note.getEtudiant() != null) {
                    etudiants.add(note.getEtudiant().getId());
                }
            }
        }

        if (totalNotes == 0) {
            return new Statistiques(0.0, 0.0, 0.0, 0);
        }

        double moyenne = roundTwo(somme / totalNotes);
        return new Statistiques(moyenne, noteMax, noteMin, etudiants.size());
    }

    @Override
    public String getMentionFromMoyenne(double moyenne) {
        if (moyenne < THRESHOLD_PASSABLE) {
            return "Insuffisant";
        }
        if (moyenne < THRESHOLD_ASSEZ_BIEN) {
            return "Passable";
        }
        if (moyenne < THRESHOLD_BIEN) {
            return "Assez Bien";
        }
        if (moyenne < THRESHOLD_TRES_BIEN) {
            return "Bien";
        }
        return "Très Bien";
    }

    private double roundTwo(double valeur) {
        return Math.round(valeur * ROUND_TWO) / ROUND_TWO;
    }
}
