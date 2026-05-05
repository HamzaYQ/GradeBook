-- Database initialization
DROP DATABASE IF EXISTS gradebook;
CREATE DATABASE gradebook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gradebook;

-- Table: classe
CREATE TABLE classe (
  id_classe INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(50) NOT NULL,
  niveau VARCHAR(30) NOT NULL,
  annee_academique VARCHAR(9) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: etudiant
CREATE TABLE etudiant (
  id_etudiant INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(50) NOT NULL,
  prenom VARCHAR(50) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  mot_de_passe VARCHAR(255) NOT NULL,
  cne VARCHAR(20) UNIQUE NOT NULL,
  id_classe INT NOT NULL,
  FOREIGN KEY (id_classe) REFERENCES classe(id_classe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: enseignant
CREATE TABLE enseignant (
  id_enseignant INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(50) NOT NULL,
  prenom VARCHAR(50) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  mot_de_passe VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: administration
CREATE TABLE administration (
  id_administration INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(50) NOT NULL,
  prenom VARCHAR(50) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  mot_de_passe VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: matiere
CREATE TABLE matiere (
  id_matiere INT PRIMARY KEY AUTO_INCREMENT,
  intitule VARCHAR(100) NOT NULL,
  coefficient FLOAT NOT NULL,
  CONSTRAINT chk_coefficient CHECK (coefficient > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: classe_matiere
CREATE TABLE classe_matiere (
  id_classe INT NOT NULL,
  id_matiere INT NOT NULL,
  PRIMARY KEY (id_classe, id_matiere),
  FOREIGN KEY (id_classe)
    REFERENCES classe(id_classe)
    ON DELETE CASCADE,
  FOREIGN KEY (id_matiere)
    REFERENCES matiere(id_matiere)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: evaluation
CREATE TABLE evaluation (
  id_evaluation INT PRIMARY KEY AUTO_INCREMENT,
  libelle VARCHAR(100) NOT NULL,
  type ENUM('EXAMEN_FINAL','CONTROLE_CONTINU','TP','PROJET') NOT NULL,
  session ENUM('NORMALE','RATTRAPAGE') NOT NULL DEFAULT 'NORMALE',
  coefficient FLOAT NOT NULL,
  CONSTRAINT chk_eval_coefficient CHECK (coefficient > 0),
  date_session DATE NOT NULL,
  id_matiere INT NOT NULL,
  id_classe INT NOT NULL,
  id_enseignant INT NOT NULL,
  FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere),
  FOREIGN KEY (id_classe) REFERENCES classe(id_classe),
  FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: note
CREATE TABLE note (
  id_etudiant INT NOT NULL,
  id_evaluation INT NOT NULL,
  valeur FLOAT NOT NULL,
  id_enseignant INT NOT NULL,
  date_saisie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_etudiant, id_evaluation),
  CONSTRAINT chk_valeur CHECK (valeur >= 0 AND valeur <= 20),
  FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant),
  FOREIGN KEY (id_evaluation) REFERENCES evaluation(id_evaluation),
  FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: presence
CREATE TABLE presence (
  id_etudiant INT NOT NULL,
  id_matiere INT NOT NULL,
  date_absence DATE NOT NULL,
  statut ENUM('JUSTIFIEE','NON_JUSTIFIEE') NOT NULL,
  source_import VARCHAR(255),
  date_import TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_etudiant, id_matiere, date_absence),
  FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant),
  FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: releve_de_notes
CREATE TABLE releve_de_notes (
  id_releve INT PRIMARY KEY AUTO_INCREMENT,
  periode ENUM('SEMESTRE_1','SEMESTRE_2','ANNUEL') NOT NULL,
  annee_academique VARCHAR(9) NOT NULL,
  moyenne_generale FLOAT,
  genere_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  id_etudiant INT NOT NULL,
  id_administration INT NOT NULL,
  FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant),
  FOREIGN KEY (id_administration) REFERENCES administration(id_administration)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: cours
CREATE TABLE cours (
  id_enseignant INT NOT NULL,
  id_classe INT NOT NULL,
  id_matiere INT NOT NULL,
  PRIMARY KEY (id_enseignant, id_classe, id_matiere),
  FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant),
  FOREIGN KEY (id_classe) REFERENCES classe(id_classe),
  FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Donnees de test: classes
INSERT INTO classe (nom, niveau, annee_academique) VALUES
  ('II-BDCC-1', 'Cycle Ingénieur', '2025-2026'),
  ('II-GLSID-1', 'Cycle Ingénieur', '2025-2026');

-- Donnees de test: matieres
INSERT INTO matiere (intitule, coefficient) VALUES
  ('Conception et POO', 3),
  ('Bases de Données', 3),
  ('Algorithmique Avancée', 2);

-- Donnees de test: classe_matiere
INSERT INTO classe_matiere (id_classe, id_matiere) VALUES
  (1, 1),
  (1, 2),
  (1, 3);

-- Donnees de test: enseignants (mot_de_passe en SHA-256)
INSERT INTO enseignant (nom, prenom, email, mot_de_passe) VALUES
  ('BENSAG', 'Hassna', 'bensag@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('ALAMI', 'Youssef', 'alami@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad');

-- Donnees de test: administration (mot_de_passe en SHA-256)
INSERT INTO administration (nom, prenom, email, mot_de_passe) VALUES
  ('ENSET', 'Scolarite', 'scolarite@enset.ma', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- Donnees de test: etudiants (mot_de_passe en SHA-256, id_classe = 1)
INSERT INTO etudiant (nom, prenom, email, mot_de_passe, cne, id_classe) VALUES
  ('YAQUINE', 'Hamza', 'yaquine@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'EE001', 1),
  ('BOUHLAOUI', 'Aymane', 'bouhlaoui@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'EE002', 1),
  ('EL HAMIDI', 'Khalid', 'elhamidi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'EE003', 1),
  ('RACHIDI', 'Sara', 'rachidi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'EE004', 1);

-- Donnees de test: affectations enseignants
INSERT INTO cours (id_enseignant, id_classe, id_matiere) VALUES
  (1, 1, 1),
  (1, 1, 2),
  (2, 1, 3);

-- Donnees de test: evaluations
INSERT INTO evaluation (libelle, type, session, coefficient, date_session, id_matiere, id_classe, id_enseignant) VALUES
  ('Controle 1', 'CONTROLE_CONTINU', 'NORMALE', 1, '2025-10-15', 1, 1, 1),
  ('Examen Final S1', 'EXAMEN_FINAL', 'NORMALE', 2, '2026-01-20', 2, 1, 1),
  ('TP Note 1', 'TP', 'NORMALE', 1, '2025-11-05', 3, 1, 2),
  ('Rattrapage POO', 'EXAMEN_FINAL', 'RATTRAPAGE', 2, '2026-02-10', 1, 1, 1);

-- Donnees de test: notes
INSERT INTO note (id_etudiant, id_evaluation, valeur, id_enseignant) VALUES
  (1, 1, 16, 1),
  (1, 2, 17, 1),
  (1, 3, 15, 2),
  (1, 4, 14, 1),
  (2, 1, 12, 1),
  (2, 2, 11, 1),
  (2, 3, 13, 2),
  (2, 4, 10, 1),
  (3, 1, 8, 1),
  (3, 2, 7, 1),
  (3, 3, 9, 2),
  (3, 4, 6, 1),
  (4, 1, 18, 1),
  (4, 2, 16, 1),
  (4, 3, 17, 2),
  (4, 4, 15, 1);

-- Donnees de test: presence
INSERT INTO presence (id_etudiant, id_matiere, date_absence, statut, source_import) VALUES
  (1, 1, '2025-10-22', 'NON_JUSTIFIEE', 'import_test.csv'),
  (1, 2, '2025-11-12', 'JUSTIFIEE', 'import_test.csv'),
  (2, 1, '2025-12-03', 'NON_JUSTIFIEE', 'import_test.csv'),
  (2, 3, '2025-10-30', 'JUSTIFIEE', 'import_test.csv'),
  (3, 2, '2026-01-08', 'NON_JUSTIFIEE', 'import_test.csv'),
  (3, 3, '2025-11-19', 'JUSTIFIEE', 'import_test.csv');
