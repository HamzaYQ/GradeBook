package com.gradebook.service.impl;

import com.gradebook.dao.IEtudiantDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.dao.IPresenceDao;
import com.gradebook.model.DonneesPresence;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Matiere;
import com.gradebook.service.IPresenceService;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PresenceServiceImpl implements IPresenceService {
    private static final String HEADER_MATRICULE = "matricule";
    private static final String HEADER_DATE = "date";
    private static final String HEADER_STATUT = "statut";
    private static final String HEADER_MATIERE = "matiere";

    private static final String STATUT_JUSTIFIEE = "JUSTIFIEE";
    private static final String STATUT_NON_JUSTIFIEE = "NON_JUSTIFIEE";

    private static final int TOTAL_SEANCES = 20;
    private static final double ROUND_ONE = 10.0;
    private static final double POURCENT_MAX = 100.0;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IPresenceDao presenceDao;
    private final IEtudiantDao etudiantDao;
    private final IMatiereDao matiereDao;

    public PresenceServiceImpl(IPresenceDao presenceDao, IEtudiantDao etudiantDao, IMatiereDao matiereDao) {
        this.presenceDao = presenceDao;
        this.etudiantDao = etudiantDao;
        this.matiereDao = matiereDao;
    }

    @Override
    public List<DonneesPresence> importerCSV(File fichier) {
        List<DonneesPresence> result = new ArrayList<>();
        if (fichier == null) {
            return result;
        }

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(fichier))) {
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                DonneesPresence presence = parseRow(row, fichier);
                if (presence != null) {
                    result.add(presence);
                }
            }
            return result;
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int importerCSVEtSauvegarder(File fichier) {
        List<DonneesPresence> presences = importerCSV(fichier);
        if (presences.isEmpty()) {
            return 0;
        }

        List<Matiere> matieres = matiereDao.findAll();
        int count = 0;

        for (DonneesPresence presence : presences) {
            Optional<Etudiant> etudiant = etudiantDao.findByMatricule(presence.getMatriculeEtudiant());
            if (etudiant.isEmpty()) {
                continue;
            }

            Matiere matiere = findMatiere(matieres, presence.getMatiere());
            if (matiere == null) {
                continue;
            }

            presenceDao.save(presence);
            count++;
        }

        return count;
    }

    @Override
    public double getTauxAbsence(int idEtudiant, int idMatiere) {
        int nbAbsences = presenceDao.countAbsencesByEtudiantAndMatiere(idEtudiant, idMatiere);
        double taux = ((double) nbAbsences / TOTAL_SEANCES) * POURCENT_MAX;
        return roundOne(taux);
    }

    @Override
    public double getTauxPresence(int idEtudiant, int idMatiere) {
        double absence = getTauxAbsence(idEtudiant, idMatiere);
        return roundOne(POURCENT_MAX - absence);
    }

    @Override
    public List<DonneesPresence> getAbsencesByEtudiant(int idEtudiant) {
        return presenceDao.findByEtudiant(idEtudiant);
    }

    @Override
    public List<DonneesPresence> getAbsencesByEtudiantAndMatiere(int idEtudiant, int idMatiere) {
        return presenceDao.findByEtudiantAndMatiere(idEtudiant, idMatiere);
    }

    private DonneesPresence parseRow(Map<String, String> row, File fichier) {
        String matricule = normalize(row.get(HEADER_MATRICULE));
        String dateValue = normalize(row.get(HEADER_DATE));
        String statutValue = normalize(row.get(HEADER_STATUT));
        String matiere = normalize(row.get(HEADER_MATIERE));

        if (matricule == null || dateValue == null || statutValue == null || matiere == null) {
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateValue, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }

        String statut = statutValue.toUpperCase(Locale.ROOT);
        if (!STATUT_JUSTIFIEE.equals(statut) && !STATUT_NON_JUSTIFIEE.equals(statut)) {
            return null;
        }

        DonneesPresence presence = new DonneesPresence();
        presence.setMatriculeEtudiant(matricule);
        presence.setDateAbsence(date);
        presence.setStatut(statut);
        presence.setMatiere(matiere);
        presence.setSourceImport(fichier != null ? fichier.getName() : null);
        return presence;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Matiere findMatiere(List<Matiere> matieres, String intitule) {
        if (matieres == null || intitule == null) {
            return null;
        }
        for (Matiere matiere : matieres) {
            if (intitule.equalsIgnoreCase(matiere.getIntitule())) {
                return matiere;
            }
        }
        return null;
    }

    private double roundOne(double valeur) {
        return Math.round(valeur * ROUND_ONE) / ROUND_ONE;
    }
}
