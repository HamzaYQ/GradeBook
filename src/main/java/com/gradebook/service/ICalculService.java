package com.gradebook.service;

import com.gradebook.model.Statistiques;

public interface ICalculService {
    double calculerMoyenneParMatiere(int idEtudiant, int idMatiere);

    double calculerMoyenneGenerale(int idEtudiant, int idClasse);

    Statistiques calculerStatistiquesEvaluation(int idEvaluation);

    Statistiques calculerStatistiquesClasse(int idClasse, int idMatiere);

    String getMentionFromMoyenne(double moyenne);
}
