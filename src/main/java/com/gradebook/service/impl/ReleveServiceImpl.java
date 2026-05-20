package com.gradebook.service.impl;

import com.gradebook.dao.IAdministrationDao;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.dao.IEvaluationDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.dao.INoteDao;
import com.gradebook.dao.IReleveDeNotesDao;
import com.gradebook.model.Administration;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.LigneReleve;
import com.gradebook.model.Matiere;
import com.gradebook.model.Note;
import com.gradebook.model.ReleveDeNotes;
import com.gradebook.model.Session;
import com.gradebook.service.ICalculService;
import com.gradebook.service.IReleveService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ReleveServiceImpl implements IReleveService {
    private static final int UNASSIGNED_ID = 0;
    private static final double ROUND_TWO = 100.0;
    private static final double NOTE_MAX = 20.0;
    private static final int EXPECTED_MATIERES_PER_SEMESTER = 6;
    private static final double VALIDATION_NOTE = 12.0;

    private static final String RESULT_VALID = "Validé";
    private static final String RESULT_RATTRAPAGE = "Rattrapage";

    private static final String EXPORT_DIR_NAME = "exports";
    private static final String FILE_PREFIX = "releve_";
    private static final String FILE_EXTENSION = ".pdf";

    private static final String TITLE_TEXT = "RELEVE DE NOTES ET RESULTATS";
    private static final String SEPARATOR_LINE = "--------------------------------------------------";

    private static final String TABLE_COL_MATIERE = "Matière";
    private static final String TABLE_COL_COEFF = "Coefficient";
    private static final String TABLE_COL_MOYENNE = "Moyenne /20";
    private static final String TABLE_COL_RESULTAT = "Résultat";

    private static final BaseColor HEADER_BG = new BaseColor(0x2E, 0x40, 0x57);
    private static final BaseColor ROW_ALT_BG = new BaseColor(0xF5, 0xF5, 0xF5);
    private static final BaseColor RESULT_VALID_BG = new BaseColor(0xD5, 0xF5, 0xE3);
    private static final BaseColor RESULT_RATTRAPAGE_BG = new BaseColor(0xFA, 0xDB, 0xD8);

    private static final int FONT_TITLE_SIZE = 18;
    private static final int FONT_NORMAL_SIZE = 12;

    private static final DateTimeFormatter FOOTER_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IReleveDeNotesDao releveDao;
    private final ICalculService calculService;
    private final IMatiereDao matiereDao;
    private final IEtudiantDao etudiantDao;
    private final IAdministrationDao administrationDao;
    private final IEvaluationDao evaluationDao;
    private final INoteDao noteDao;

    public ReleveServiceImpl(IReleveDeNotesDao releveDao, ICalculService calculService, IMatiereDao matiereDao,
                             IEtudiantDao etudiantDao, IAdministrationDao administrationDao,
                             IEvaluationDao evaluationDao, INoteDao noteDao) {
        this.releveDao = releveDao;
        this.calculService = calculService;
        this.matiereDao = matiereDao;
        this.etudiantDao = etudiantDao;
        this.administrationDao = administrationDao;
        this.evaluationDao = evaluationDao;
        this.noteDao = noteDao;
    }

    @Override
    public ReleveDeNotes genererReleve(int idEtudiant, int semestre, String anneeAcademique, int idAdmin) {
        Etudiant etudiant = etudiantDao.findById(idEtudiant)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        Administration admin = administrationDao.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));
        Classe classe = etudiant.getClasse();
        if (classe == null) {
            throw new IllegalStateException("Classe introuvable pour l'étudiant");
        }

        List<Matiere> matieres = matiereDao.findByClasse(classe.getId());
        if (matieres.size() != EXPECTED_MATIERES_PER_SEMESTER) {
            throw new IllegalStateException(
                    "Configuration invalide : " + EXPECTED_MATIERES_PER_SEMESTER +
                            " matières attendues pour le semestre " + semestre
            );
        }

        Optional<ReleveDeNotes> releveSession1 =
                releveDao.findByEtudiantAndSemestreAndSession(idEtudiant, semestre, 1);

        int session;
        List<LigneReleve> lignes;
        if (releveSession1.isEmpty()) {
            List<String> matieresManquantes = findMatieresSansNotes(idEtudiant, semestre, matieres, Session.NORMALE);
            if (!matieresManquantes.isEmpty()) {
                throw new IllegalStateException(
                        "Impossible de générer le relevé de " + etudiant.getNom() + " " + etudiant.getPrenom() +
                                " : notes manquantes pour les matières : " + String.join(", ", matieresManquantes)
                );
            }
            session = 1;
            lignes = buildLignesPourSemestre(etudiant, semestre, session);
        } else {
            List<LigneReleve> lignesS1 = releveSession1.get().getLignes();
            if (lignesS1 == null || lignesS1.isEmpty()) {
                lignesS1 = buildLignesPourSemestre(etudiant, semestre, 1);
            }
            if (!hasMoyenneInferieure(lignesS1, VALIDATION_NOTE)) {
                throw new IllegalStateException(
                        "Cet étudiant a validé tous ses semestres en Session 1. Pas de Session 2 nécessaire."
                );
            }
            if (!hasRattrapageNotes(idEtudiant, semestre, classe.getId(), matieres)) {
                throw new IllegalStateException("Aucune note de rattrapage trouvée pour cet étudiant.");
            }
            session = 2;
            lignes = buildLignesPourSemestre(etudiant, semestre, session);
        }

        ReleveDeNotes releve = new ReleveDeNotes();
        releve.setEtudiant(etudiant);
        releve.setGenereParAdmin(admin);
        releve.setSemestre(semestre);
        releve.setSession(session);
        releve.setAnneeAcademique(anneeAcademique);
        releve.setLignes(lignes);

        double moyenneGenerale = calculerMoyenneGenerale(lignes);
        releve.setMoyenneGenerale(moyenneGenerale);

        String resultat = resolveResultat(lignes);
        releve.setResultat(resultat);

        releveDao.create(releve);
        return releve;
    }

    @Override
    public List<ReleveDeNotes> genererRelevesPourClasse(int idClasse, int semestre, String anneeAcademique, int idAdmin) {
        List<Etudiant> etudiants = etudiantDao.findByClasse(idClasse);
        List<ReleveDeNotes> relevesGeneres = new ArrayList<>();
        List<String> relevesIgnores = new ArrayList<>();

        for (Etudiant etudiant : etudiants) {
            try {
                ReleveDeNotes releve = genererReleve(etudiant.getId(), semestre, anneeAcademique, idAdmin);
                relevesGeneres.add(releve);
            } catch (IllegalStateException e) {
                relevesIgnores.add(etudiant.getNom() + " " + etudiant.getPrenom() + " : " + e.getMessage());
            }
        }

        if (!relevesIgnores.isEmpty()) {
            for (String message : relevesIgnores) {
                System.out.println(message);
            }
        }

        return relevesGeneres;
    }

    @Override
    public List<ReleveDeNotes> getRelevesByEtudiant(int idEtudiant) {
        return releveDao.findByEtudiant(idEtudiant);
    }

    @Override
    public Optional<ReleveDeNotes> getReleveByEtudiantAndSemestre(int idEtudiant, int semestre) {
        return releveDao.findByEtudiantAndSemestre(idEtudiant, semestre);
    }

    @Override
    public File exporterRelevePDF(ReleveDeNotes releve) {
        File exportDir = new File(EXPORT_DIR_NAME);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        Etudiant etudiant = resolveEtudiant(releve.getEtudiant());
        String cne = etudiant != null ? etudiant.getCne() : "inconnu";
        String periode = formatSemestreCode(releve.getSemestre());
        String annee = releve.getAnneeAcademique() != null ? releve.getAnneeAcademique() : "NA";

        String fileName = FILE_PREFIX + cne + "_" + periode + "_" + annee + FILE_EXTENSION;
        File file = new File(exportDir, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, out);
                document.open();

                Font fontTitle = new Font(Font.FontFamily.HELVETICA, FONT_TITLE_SIZE, Font.BOLD);
                Font fontBold = new Font(Font.FontFamily.HELVETICA, FONT_NORMAL_SIZE, Font.BOLD);
                Font fontNormal = new Font(Font.FontFamily.HELVETICA, FONT_NORMAL_SIZE);

                Paragraph title = new Paragraph(TITLE_TEXT, fontTitle);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                Paragraph sessionTitle = new Paragraph("SESSION " + releve.getSession(), fontBold);
                sessionTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(sessionTitle);
                document.add(new Paragraph(SEPARATOR_LINE));

                String nomComplet = etudiant != null ? etudiant.getNom() + " " + etudiant.getPrenom() : "";
                Classe classe = etudiant != null ? etudiant.getClasse() : null;
                String classeNom = classe != null ? classe.getNom() : "";

                PdfPTable infosTable = new PdfPTable(2);
                infosTable.setWidthPercentage(100);
                infosTable.addCell(createInfoCell("Nom complet :", fontBold));
                infosTable.addCell(createInfoCell(nomComplet, fontNormal));
                infosTable.addCell(createInfoCell("CNE :", fontBold));
                infosTable.addCell(createInfoCell(cne, fontNormal));
                infosTable.addCell(createInfoCell("Classe :", fontBold));
                infosTable.addCell(createInfoCell(classeNom, fontNormal));
                infosTable.addCell(createInfoCell("Année académique :", fontBold));
                infosTable.addCell(createInfoCell(annee, fontNormal));
                infosTable.addCell(createInfoCell("Semestre :", fontBold));
                infosTable.addCell(createInfoCell(formatSemestreLabel(releve.getSemestre()), fontNormal));

                document.add(infosTable);
                document.add(new Paragraph(SEPARATOR_LINE));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);

                table.addCell(createHeaderCell(TABLE_COL_MATIERE, fontBold));
                table.addCell(createHeaderCell(TABLE_COL_COEFF, fontBold));
                table.addCell(createHeaderCell(TABLE_COL_MOYENNE, fontBold));
                table.addCell(createHeaderCell(TABLE_COL_RESULTAT, fontBold));

                List<LigneReleve> lignes = releve.getLignes();
                if (lignes == null || lignes.isEmpty()) {
                    int session = releve.getSession() == 2 ? 2 : 1;
                    lignes = buildLignesPourSemestre(etudiant, releve.getSemestre(), session);
                    releve.setLignes(lignes);
                }
                boolean alt = false;
                for (LigneReleve ligne : lignes) {
                    String intitule = ligne.getMatiere() != null ? ligne.getMatiere().getIntitule() : "";
                    String coeff = formatTwo(ligne.getCoefficient());
                    String moyenne = formatTwo(ligne.getMoyenneMatiere()) + "/20";
                    String resultat = ligne.getMoyenneMatiere() >= VALIDATION_NOTE ? RESULT_VALID : RESULT_RATTRAPAGE;

                    BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
                    table.addCell(createBodyCell(intitule, fontNormal, bg));
                    table.addCell(createBodyCell(coeff, fontNormal, bg));
                    table.addCell(createBodyCell(moyenne, fontNormal, bg));
                    table.addCell(createBodyCell(resultat, fontNormal, bg));
                    alt = !alt;
                }

                document.add(table);
                document.add(new Paragraph(" "));

                double moyenneGeneraleValue = releve.getMoyenneGenerale();
                if (moyenneGeneraleValue <= 0.0 && etudiant != null) {
                    Optional<ReleveDeNotes> persisted = releveDao.findByEtudiantAndSemestreAndSession(
                        etudiant.getId(), releve.getSemestre(), releve.getSession()
                    );
                    if (persisted.isPresent()) {
                    moyenneGeneraleValue = persisted.get().getMoyenneGenerale();
                    }
                }
                String moyenneGenerale = formatTwo(moyenneGeneraleValue);
                Paragraph resume = new Paragraph(
                    "Résultat d'admission session " + releve.getSession() + " : " + moyenneGenerale + "/20",
                    fontBold
                );
                resume.setSpacingBefore(8f);
                // add extra vertical space between the admission result and the footer
                resume.setSpacingAfter(24f);
                document.add(resume);

                LocalDateTime genereLe = releve.getGenereLe() != null ? releve.getGenereLe() : LocalDateTime.now();
                String footerText = "\n\n\n\n\n Fait à Mohammedia, le " + genereLe.format(FOOTER_FORMATTER) +
                    "\nDirecteur de l'Ecole Normale Supérieure de l'Enseignement Technique - Mohammedia";

                Paragraph footer = new Paragraph(footerText, fontNormal);
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                return file;
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        Font headerFont = new Font(font.getFamily(), font.getSize(), font.getStyle(), BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Paragraph(text, headerFont));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(HEADER_BG);
        cell.setPadding(6f);
        return cell;
    }

    private PdfPCell createInfoCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        return cell;
    }

    private List<LigneReleve> buildLignesPourSemestre(Etudiant etudiant, int semestre, int session) {
        List<LigneReleve> lignes = new ArrayList<>();
        if (etudiant == null || etudiant.getClasse() == null) {
            return lignes;
        }

        int classeId = etudiant.getClasse().getId();
        List<Matiere> matieres = matiereDao.findByClasse(classeId);
        if (matieres.size() != EXPECTED_MATIERES_PER_SEMESTER) {
            throw new IllegalStateException(
                    "Configuration invalide : " + EXPECTED_MATIERES_PER_SEMESTER +
                            " matières attendues pour le semestre " + semestre
            );
        }
        for (Matiere matiere : matieres) {
            List<Note> notes = getNotesPourSession(etudiant.getId(), matiere.getId(), semestre, session);
            double moyenne = calculerMoyenne(notes);

            LigneReleve ligne = new LigneReleve();
            ligne.setMatiere(matiere);
            ligne.setMoyenneMatiere(moyenne);
            ligne.setCoefficient(matiere.getCoefficient());
            lignes.add(ligne);
        }

        return lignes;
    }

    private List<String> findMatieresSansNotes(int idEtudiant, int semestre, List<Matiere> matieres, Session session) {
        List<String> matieresManquantes = new ArrayList<>();
        for (Matiere matiere : matieres) {
            List<Note> notes = noteDao.findByEtudiantAndMatiereAndSemestre(idEtudiant, matiere.getId(), semestre);
            List<Note> notesSession = filterNotesBySession(notes, session);
            if (notesSession.isEmpty()) {
                matieresManquantes.add(matiere.getIntitule());
            }
        }
        return matieresManquantes;
    }

    private boolean hasRattrapageNotes(int idEtudiant, int semestre, int idClasse, List<Matiere> matieres) {
        for (Matiere matiere : matieres) {
            List<Evaluation> evaluations = evaluationDao.findByClasseAndMatiereBySemestre(
                    idClasse, matiere.getId(), semestre
            );
            for (Evaluation evaluation : evaluations) {
                if (evaluation.getSession() == Session.RATTRAPAGE &&
                        noteDao.findByEtudiantAndEvaluation(idEtudiant, evaluation.getId()).isPresent()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMoyenneInferieure(List<LigneReleve> lignes, double seuil) {
        for (LigneReleve ligne : lignes) {
            if (ligne.getMoyenneMatiere() < seuil) {
                return true;
            }
        }
        return false;
    }

    private String resolveResultat(List<LigneReleve> lignes) {
        return hasMoyenneInferieure(lignes, VALIDATION_NOTE) ? RESULT_RATTRAPAGE : RESULT_VALID;
    }

    private double calculerMoyenneGenerale(List<LigneReleve> lignes) {
        double sommeMoyennes = 0.0;
        double sommeCoefsMatieres = 0.0;
        for (LigneReleve ligne : lignes) {
            sommeMoyennes += ligne.getMoyenneMatiere() * ligne.getCoefficient();
            sommeCoefsMatieres += ligne.getCoefficient();
        }
        return sommeCoefsMatieres > 0
                ? Math.round((sommeMoyennes / sommeCoefsMatieres) * ROUND_TWO) / ROUND_TWO
                : 0.0;
    }

    private double calculerMoyenne(List<Note> notes) {
        double sommeNotes = 0.0;
        double sommeCoefs = 0.0;
        for (Note note : notes) {
            double coef = note.getEvaluation() != null ? note.getEvaluation().getCoefficient() : 0.0;
            sommeNotes += note.getValeur() * coef;
            sommeCoefs += coef;
        }
        return sommeCoefs > 0
                ? Math.round((sommeNotes / sommeCoefs) * ROUND_TWO) / ROUND_TWO
                : 0.0;
    }

    private List<Note> getNotesPourSession(int idEtudiant, int idMatiere, int semestre, int session) {
        List<Note> notes = noteDao.findByEtudiantAndMatiereAndSemestre(idEtudiant, idMatiere, semestre);
        Session sessionFilter = session == 2 ? Session.RATTRAPAGE : Session.NORMALE;
        List<Note> filtered = filterNotesBySession(notes, sessionFilter);
        if (session == 2 && filtered.isEmpty()) {
            filtered = filterNotesBySession(notes, Session.NORMALE);
        }
        return filtered;
    }

    private List<Note> filterNotesBySession(List<Note> notes, Session session) {
        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.getEvaluation() != null && note.getEvaluation().getSession() == session) {
                filtered.add(note);
            }
        }
        return filtered;
    }

    private Etudiant resolveEtudiant(Etudiant etudiant) {
        if (etudiant == null) {
            return null;
        }
        if (etudiant.getClasse() != null) {
            return etudiant;
        }
        return etudiantDao.findById(etudiant.getId()).orElse(etudiant);
    }

    private Administration resolveAdministration(Administration admin) {
        if (admin == null) {
            return null;
        }
        if (admin.getPrenom() != null && admin.getNom() != null) {
            return admin;
        }
        return administrationDao.findById(admin.getId()).orElse(admin);
    }

    private PdfPCell createBodyCell(String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        return cell;
    }

    private String formatTwo(double valeur) {
        double arrondi = Math.round(valeur * ROUND_TWO) / ROUND_TWO;
        return String.format(Locale.US, "%.2f", arrondi);
    }

    private String formatSemestreLabel(int semestre) {
        return "Semestre " + semestre;
    }

    private String formatSemestreCode(int semestre) {
        return "S" + semestre;
    }
}
