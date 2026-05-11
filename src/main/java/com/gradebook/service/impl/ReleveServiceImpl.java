package com.gradebook.service.impl;

import com.gradebook.dao.IAdministrationDao;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.dao.IReleveDeNotesDao;
import com.gradebook.model.Administration;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;
import com.gradebook.model.LigneReleve;
import com.gradebook.model.Matiere;
import com.gradebook.model.ReleveDeNotes;
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

    private static final String EXPORT_DIR_NAME = "exports";
    private static final String FILE_PREFIX = "releve_";
    private static final String FILE_EXTENSION = ".pdf";

    private static final String TITLE_TEXT = "GradeBook — Relevé de Notes Officiel";
    private static final String SEPARATOR_LINE = "--------------------------------------------------";

    private static final String TABLE_COL_MATIERE = "Matière";
    private static final String TABLE_COL_COEFF = "Coefficient";
    private static final String TABLE_COL_MOYENNE = "Moyenne";
    private static final String TABLE_COL_MENTION = "Mention";

    private static final BaseColor HEADER_BG = new BaseColor(0x2E, 0x40, 0x57);
    private static final BaseColor ROW_ALT_BG = new BaseColor(0xF5, 0xF5, 0xF5);

    private static final int FONT_TITLE_SIZE = 16;
    private static final int FONT_NORMAL_SIZE = 12;

    private static final DateTimeFormatter FOOTER_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IReleveDeNotesDao releveDao;
    private final ICalculService calculService;
    private final IMatiereDao matiereDao;
    private final IEtudiantDao etudiantDao;
    private final IAdministrationDao administrationDao;

    public ReleveServiceImpl(IReleveDeNotesDao releveDao, ICalculService calculService, IMatiereDao matiereDao,
                             IEtudiantDao etudiantDao, IAdministrationDao administrationDao) {
        this.releveDao = releveDao;
        this.calculService = calculService;
        this.matiereDao = matiereDao;
        this.etudiantDao = etudiantDao;
        this.administrationDao = administrationDao;
    }

    @Override
    public ReleveDeNotes genererReleve(int idEtudiant, int semestre, String anneeAcademique, int idAdmin) {
        Optional<Etudiant> etudiantOpt = etudiantDao.findById(idEtudiant);
        if (etudiantOpt.isEmpty()) {
            throw new IllegalStateException("Etudiant introuvable");
        }

        Optional<Administration> adminOpt = administrationDao.findById(idAdmin);
        if (adminOpt.isEmpty()) {
            throw new IllegalStateException("Administration introuvable");
        }

        Etudiant etudiant = etudiantOpt.get();
        Administration admin = adminOpt.get();
        Classe classe = etudiant.getClasse();
        if (classe == null) {
            throw new IllegalStateException("Classe introuvable pour l'étudiant");
        }

        List<Matiere> matieres = matiereDao.findByClasse(classe.getId());

        ReleveDeNotes releve = new ReleveDeNotes(UNASSIGNED_ID, semestre, anneeAcademique, etudiant, admin);
        for (Matiere matiere : matieres) {
            double moyenne = calculService.calculerMoyenneParMatiere(idEtudiant, matiere.getId(), semestre);
            if (moyenne > 0.0) {
                LigneReleve ligne = new LigneReleve(matiere, moyenne, matiere.getCoefficient());
                releve.addLigne(ligne);
            }
        }

        double moyenneGenerale = calculService.calculerMoyenneGenerale(idEtudiant, classe.getId(), semestre);
        releve.setMoyenneGenerale(moyenneGenerale);

        releveDao.create(releve);
        return releve;
    }

    @Override
    public List<ReleveDeNotes> genererRelevesPourClasse(int idClasse, int semestre, String anneeAcademique, int idAdmin) {
        List<Etudiant> etudiants = etudiantDao.findByClasse(idClasse);
        List<ReleveDeNotes> releves = new ArrayList<>();

        for (Etudiant etudiant : etudiants) {
            releves.add(genererReleve(etudiant.getId(), semestre, anneeAcademique, idAdmin));
        }

        return releves;
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

        Etudiant etudiant = releve.getEtudiant();
        String cne = etudiant != null ? etudiant.getCne() : "inconnu";
        String periode = formatSemestreCode(releve.getSemestre());
        String annee = releve.getAnneeAcademique() != null ? releve.getAnneeAcademique() : "NA";

        String fileName = FILE_PREFIX + cne + "_" + periode + "_" + annee + FILE_EXTENSION;
        File file = new File(exportDir, fileName);

        Document document = new Document();
        try (FileOutputStream out = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = new Font(Font.FontFamily.HELVETICA, FONT_TITLE_SIZE, Font.BOLD);
            Font fontBold = new Font(Font.FontFamily.HELVETICA, FONT_NORMAL_SIZE, Font.BOLD);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, FONT_NORMAL_SIZE);

            Paragraph title = new Paragraph(TITLE_TEXT, fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(SEPARATOR_LINE));

            String nomComplet = etudiant != null ? etudiant.getNom() + " " + etudiant.getPrenom() : "";
            Classe classe = etudiant != null ? etudiant.getClasse() : null;
            String classeNom = classe != null ? classe.getNom() : "";

            Paragraph infos = new Paragraph(
                    "Nom complet : " + nomComplet + "\n" +
                    "CNE : " + cne + "\n" +
                    "Classe : " + classeNom + "\n" +
                        "Année académique : " + annee + "\n" +
                        "Semestre : " + formatSemestreLabel(releve.getSemestre()),
                    fontNormal
            );
            document.add(infos);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            table.addCell(createHeaderCell(TABLE_COL_MATIERE, fontBold));
            table.addCell(createHeaderCell(TABLE_COL_COEFF, fontBold));
            table.addCell(createHeaderCell(TABLE_COL_MOYENNE, fontBold));
            table.addCell(createHeaderCell(TABLE_COL_MENTION, fontBold));

            List<LigneReleve> lignes = releve.getLignes() != null ? releve.getLignes() : new ArrayList<>();
            boolean alt = false;
            for (LigneReleve ligne : lignes) {
                String intitule = ligne.getMatiere() != null ? ligne.getMatiere().getIntitule() : "";
                String coeff = formatTwo(ligne.getCoefficient());
                String moyenne = formatTwo(ligne.getMoyenneMatiere());
                String mention = calculService.getMentionFromMoyenne(ligne.getMoyenneMatiere());

                BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
                table.addCell(createBodyCell(intitule, fontNormal, bg));
                table.addCell(createBodyCell(coeff, fontNormal, bg));
                table.addCell(createBodyCell(moyenne, fontNormal, bg));
                table.addCell(createBodyCell(mention, fontNormal, bg));
                alt = !alt;
            }

            document.add(table);
            document.add(new Paragraph(" "));

            String moyenneGenerale = formatTwo(releve.getMoyenneGenerale());
            String mentionGenerale = calculService.getMentionFromMoyenne(releve.getMoyenneGenerale());
            Paragraph resume = new Paragraph(
                    "Moyenne Générale : " + moyenneGenerale + "/" + formatTwo(NOTE_MAX) + " — " + mentionGenerale,
                    fontBold
            );
            document.add(resume);

            Administration admin = releve.getGenereParAdmin();
            String adminNom = admin != null ? admin.getNom() : "";
            String adminPrenom = admin != null ? admin.getPrenom() : "";
            LocalDateTime genereLe = releve.getGenereLe() != null ? releve.getGenereLe() : LocalDateTime.now();
            String footerText = "Généré le " + genereLe.format(FOOTER_FORMATTER) + " par " +
                    (adminNom + " " + adminPrenom).trim() + " | GradeBook";

            Paragraph footer = new Paragraph(footerText, fontNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            return file;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(HEADER_BG);
        cell.setPadding(6f);
        cell.getPhrase().getFont().setColor(BaseColor.WHITE);
        return cell;
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
        return "SEMESTRE_" + semestre;
    }
}
