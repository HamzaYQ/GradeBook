package com.gradebook.service;

import com.gradebook.model.DonneesPresence;

import java.io.File;
import java.util.List;

public interface IPresenceService {
    List<DonneesPresence> importerCSV(File fichier);

    int importerCSVEtSauvegarder(File fichier);

    double getTauxAbsence(int idEtudiant, int idMatiere);

    double getTauxPresence(int idEtudiant, int idMatiere);

    List<DonneesPresence> getAbsencesByEtudiant(int idEtudiant);

    List<DonneesPresence> getAbsencesByEtudiantAndMatiere(int idEtudiant, int idMatiere);
}
