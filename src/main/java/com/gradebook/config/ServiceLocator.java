package com.gradebook.config;

import com.gradebook.dao.IAdministrationDao;
import com.gradebook.dao.IClasseDao;
import com.gradebook.dao.IClasseMatiereDao;
import com.gradebook.dao.ICoursDao;
import com.gradebook.dao.IEnseignantDao;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.dao.IEvaluationDao;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.dao.INoteDao;
import com.gradebook.dao.IPresenceDao;
import com.gradebook.dao.IReleveDeNotesDao;
import com.gradebook.dao.impl.AdministrationDaoImpl;
import com.gradebook.dao.impl.ClasseDaoImpl;
import com.gradebook.dao.impl.ClasseMatiereDaoImpl;
import com.gradebook.dao.impl.CoursDaoImpl;
import com.gradebook.dao.impl.EnseignantDaoImpl;
import com.gradebook.dao.impl.EtudiantDaoImpl;
import com.gradebook.dao.impl.EvaluationDaoImpl;
import com.gradebook.dao.impl.MatiereDaoImpl;
import com.gradebook.dao.impl.NoteDaoImpl;
import com.gradebook.dao.impl.PresenceDaoImpl;
import com.gradebook.dao.impl.ReleveDeNotesDaoImpl;
import com.gradebook.service.IAuthService;
import com.gradebook.service.ICalculService;
import com.gradebook.service.INoteService;
import com.gradebook.service.IPresenceService;
import com.gradebook.service.IReferentielService;
import com.gradebook.service.IReleveService;
import com.gradebook.service.impl.AuthServiceImpl;
import com.gradebook.service.impl.CalculServiceImpl;
import com.gradebook.service.impl.NoteServiceImpl;
import com.gradebook.service.impl.PresenceServiceImpl;
import com.gradebook.service.impl.ReferentielServiceImpl;
import com.gradebook.service.impl.ReleveServiceImpl;

public final class ServiceLocator {
    private static IClasseDao classeDao;
    private static IClasseMatiereDao classeMatiereDao;
    private static IMatiereDao matiereDao;
    private static IEtudiantDao etudiantDao;
    private static IEnseignantDao enseignantDao;
    private static IAdministrationDao administrationDao;
    private static IEvaluationDao evaluationDao;
    private static INoteDao noteDao;
    private static IPresenceDao presenceDao;
    private static IReleveDeNotesDao releveDao;
    private static ICoursDao coursDao;

    private static IAuthService authService;
    private static INoteService noteService;
    private static ICalculService calculService;
    private static IReferentielService referentielService;
    private static IPresenceService presenceService;
    private static IReleveService releveService;

    private ServiceLocator() {
    }

    private static void initDaos() {
        if (classeDao != null) {
            return;
        }
        classeDao = new ClasseDaoImpl();
        classeMatiereDao = new ClasseMatiereDaoImpl();
        matiereDao = new MatiereDaoImpl();
        etudiantDao = new EtudiantDaoImpl();
        enseignantDao = new EnseignantDaoImpl();
        administrationDao = new AdministrationDaoImpl();
        evaluationDao = new EvaluationDaoImpl();
        noteDao = new NoteDaoImpl();
        presenceDao = new PresenceDaoImpl();
        releveDao = new ReleveDeNotesDaoImpl();
        coursDao = new CoursDaoImpl();
    }

    private static void initServices() {
        if (authService != null) {
            return;
        }
        initDaos();
        authService = new AuthServiceImpl(etudiantDao, enseignantDao, administrationDao);
        noteService = new NoteServiceImpl(noteDao, evaluationDao, coursDao);
        calculService = new CalculServiceImpl(noteDao, evaluationDao, matiereDao);
        referentielService = new ReferentielServiceImpl(
            classeDao,
            matiereDao,
            enseignantDao,
            etudiantDao,
            coursDao,
            classeMatiereDao
        );
        presenceService = new PresenceServiceImpl(presenceDao, etudiantDao, matiereDao);
        releveService = new ReleveServiceImpl(
            releveDao,
            calculService,
            matiereDao,
            etudiantDao,
            administrationDao,
            evaluationDao,
            noteDao
        );
    }

    public static IAuthService getAuthService() {
        initServices();
        return authService;
    }

    public static INoteService getNoteService() {
        initServices();
        return noteService;
    }

    public static ICalculService getCalculService() {
        initServices();
        return calculService;
    }

    public static IReferentielService getReferentielService() {
        initServices();
        return referentielService;
    }

    public static IPresenceService getPresenceService() {
        initServices();
        return presenceService;
    }

    public static IReleveService getReleveService() {
        initServices();
        return releveService;
    }

    public static IEtudiantDao getEtudiantDao() {
        initDaos();
        return etudiantDao;
    }

    public static IEnseignantDao getEnseignantDao() {
        initDaos();
        return enseignantDao;
    }

    public static IEvaluationDao getEvaluationDao() {
        initDaos();
        return evaluationDao;
    }

    public static IMatiereDao getMatiereDao() {
        initDaos();
        return matiereDao;
    }

    public static IClasseDao getClasseDao() {
        initDaos();
        return classeDao;
    }

    public static IClasseMatiereDao getClasseMatiereDao() {
        initDaos();
        return classeMatiereDao;
    }
}
