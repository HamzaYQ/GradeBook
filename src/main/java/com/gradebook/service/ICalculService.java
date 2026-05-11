package com.gradebook.service;

import com.gradebook.model.Statistiques;

public interface ICalculService {
    double calculerMoyenneParMatiere(int idEtudiant, int idMatiere, int semestre);

    double calculerMoyenneGenerale(int idEtudiant, int idClasse, int semestre);

    double calculerMoyenneAnnuelle(int idEtudiant, int idClasse);

    Statistiques calculerStatistiquesEvaluation(int idEvaluation);

    Statistiques calculerStatistiquesClasse(int idClasse, int idMatiere, int semestre);

    String getMentionFromMoyenne(double moyenne);
}
