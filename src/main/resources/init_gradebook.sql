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
  semestre TINYINT NOT NULL,
  CONSTRAINT chk_semestre_eval CHECK (semestre IN (1, 2)),
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
  semestre TINYINT NOT NULL,
  CONSTRAINT chk_semestre_releve CHECK (semestre IN (1, 2)),
  session TINYINT NOT NULL DEFAULT 1,
  CONSTRAINT chk_session_releve CHECK (session IN (1, 2)),
  annee_academique VARCHAR(9) NOT NULL,
  moyenne_generale FLOAT,
  resultat ENUM('Validé','Rattrapage') NOT NULL DEFAULT 'Validé',
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
  semestre TINYINT NOT NULL,
  CONSTRAINT chk_semestre CHECK (semestre IN (1, 2)),
  PRIMARY KEY (id_enseignant, id_classe, id_matiere, semestre),
  FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant),
  FOREIGN KEY (id_classe) REFERENCES classe(id_classe),
  FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Donnees de test: classes
INSERT INTO classe (nom, niveau, annee_academique) VALUES
  ('II-BDCC', 'Cycle Ingénieur', '2025-2026'),
  ('II-GLSID', 'Cycle Ingénieur', '2025-2026'),
  ('II-CCN', 'Cycle Ingénieur', '2025-2026');

-- Donnees de test: matieres
INSERT INTO matiere (intitule, coefficient) VALUES
  ('Conception et POO', 3),
  ('Bases de Données', 3),
  ('Génie Logiciel', 3),
  ('Développement Web', 2),
  ('Réseaux', 2),
  ('Systèmes d''Exploitation', 2),
  ('IA et Data Mining', 2),
  ('Cloud Computing', 2),
  ('Sécurité Informatique', 2),
  ('DevOps', 2),
  ('Big Data', 3);

-- Donnees de test: classe_matiere (6 matieres par classe)
INSERT INTO classe_matiere (id_classe, id_matiere) VALUES
  (1, 1),
  (1, 2),
  (1, 7),
  (1, 8),
  (1, 10),
  (1, 11),
  (2, 1),
  (2, 2),
  (2, 3),
  (2, 4),
  (2, 9),
  (2, 10),
  (3, 2),
  (3, 4),
  (3, 5),
  (3, 6),
  (3, 8),
  (3, 9);

-- Donnees de test: enseignants (mot_de_passe en SHA-256)
INSERT INTO enseignant (nom, prenom, email, mot_de_passe) VALUES
  ('EL AMRANI', 'Salma', 'elamrani@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('BENSAG', 'Hassna', 'bensag@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('ALAMI', 'Youssef', 'alami@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('ZIANE', 'Omar', 'ziane@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('HADDAD', 'Lina', 'haddad@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('EL HAFID', 'Mehdi', 'elhafid@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('KHALIL', 'Nadia', 'khalil@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('SAIDI', 'Karim', 'saidi@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('MANSOURI', 'Hiba', 'mansouri@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad'),
  ('BELKACEM', 'Rachid', 'belkacem@enset.ma', '475ba33c72a6f1dd3495197a80d6d748849069e6f87e90f41f6d661c65e885ad');

-- Donnees de test: administration (mot_de_passe en SHA-256)
INSERT INTO administration (nom, prenom, email, mot_de_passe) VALUES
  ('ENSET', 'Scolarite', 'scolarite@enset.ma', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- Donnees de test: etudiants (mot_de_passe en SHA-256)
INSERT INTO etudiant (nom, prenom, email, mot_de_passe, cne, id_classe) VALUES
  ('Benchekroun', 'Yassine', 'benchekroun@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000001', 1),
  ('Chikhi', 'Amal', 'chikhi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000002', 1),
  ('Rifi', 'Ahmed', 'rifi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000003', 1),
  ('Haddadi', 'Sara', 'haddadi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000004', 1),
  ('Berrada', 'Karim', 'berrada@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000005', 1),
  ('Zahiri', 'Salma', 'zahiri@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000006', 1),
  ('Oubari', 'Rayan', 'oubari@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000007', 1),
  ('Amrani', 'Nora', 'amrani@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000008', 1),
  ('Kabbaj', 'Samir', 'kabbaj@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000009', 1),
  ('Fassi', 'Laila', 'fassi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000010', 1),
  ('Moutawakil', 'Reda', 'moutawakil@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000011', 1),
  ('Boulahya', 'Hajar', 'boulahya@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000012', 1),
  ('Taibi', 'Mehdi', 'taibi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000013', 1),
  ('Lahlou', 'Ines', 'lahlou@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000014', 1),
  ('El Idrissi', 'Sanaa', 'elidrissi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000015', 1),
  ('Touil', 'Nabil', 'touil@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000016', 1),
  ('Bouazza', 'Rania', 'bouazza@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000017', 1),
  ('Tazi', 'Ismail', 'tazi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000018', 1),
  ('Ezzaki', 'Aya', 'ezzaki@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000019', 1),
  ('Ghali', 'Omar', 'ghali@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000020', 1),
  ('Benmoussa', 'Adam', 'benmoussa@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000021', 2),
  ('Zahraoui', 'Lina', 'zahraoui@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000022', 2),
  ('Najmi', 'Younes', 'najmi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000023', 2),
  ('Rouissi', 'Sara', 'rouissi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000024', 2),
  ('Messaoudi', 'Bilal', 'messaoudi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000025', 2),
  ('El Khatib', 'Leila', 'elkhatib@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000026', 2),
  ('Charif', 'Hamza', 'charif@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000027', 2),
  ('Hamdouni', 'Samira', 'hamdouni@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000028', 2),
  ('Benjelloun', 'Anas', 'benjelloun@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000029', 2),
  ('Idrissi', 'Yasmina', 'idrissi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000030', 2),
  ('El Azzouzi', 'Walid', 'elazzouzi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000031', 2),
  ('Qacemi', 'Hiba', 'qacemi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000032', 2),
  ('Ouali', 'Rachid', 'ouali@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000033', 2),
  ('Bennis', 'Manal', 'bennis@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000034', 2),
  ('Azizi', 'Soufiane', 'azizi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000035', 2),
  ('Kouta', 'Salma', 'kouta@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000036', 2),
  ('Haddouch', 'Nizar', 'haddouch@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000037', 2),
  ('Saad', 'Iman', 'saad@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000038', 2),
  ('Naciri', 'Yassine', 'naciri@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000039', 2),
  ('Akrimi', 'Hajar', 'akrimi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000040', 2),
  ('Toumi', 'Omar', 'toumi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000041', 3),
  ('Maazouz', 'Fatima', 'maazouz@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000042', 3),
  ('Birouk', 'Youssef', 'birouk@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000043', 3),
  ('El Ghazali', 'Sanaa', 'elghazali@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000044', 3),
  ('Merzouki', 'Reda', 'merzouki@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000045', 3),
  ('Rami', 'Ilias', 'rami@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000046', 3),
  ('Chraibi', 'Hind', 'chraibi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000047', 3),
  ('Lahmidi', 'Ilham', 'lahmidi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000048', 3),
  ('Bousselham', 'Tarik', 'bousselham@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000049', 3),
  ('Omari', 'Zine', 'omari@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000050', 3),
  ('Kabbouri', 'Amal', 'kabbouri@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000051', 3),
  ('Boussif', 'Karim', 'boussif@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000052', 3),
  ('El Fassi', 'Nour', 'elfassi@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000053', 3),
  ('Ait Ouarjou', 'Rayan', 'aitouarjou@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000054', 3),
  ('Mkerssou', 'Houssam', 'mkerssou@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000055', 3),
  ('Alami', 'Sami', 'alami@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000056', 3),
  ('Nourredine', 'Salma', 'nourredine@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000057', 3),
  ('Kharroub', 'Bilal', 'kharroub@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000058', 3),
  ('El Kebir', 'Nora', 'elkebir@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000059', 3),
  ('Ziane', 'Rayan', 'ziane@enset.ma', '36432aa0a54a06c13ca2ff16cef78ca66e1cd5fa869f36791a79bc4f4c5d8120', 'R100000060', 3);

-- Donnees de test: affectations enseignants
INSERT INTO cours (id_enseignant, id_classe, id_matiere, semestre) VALUES
  (1, 1, 1, 1),
  (1, 1, 2, 1),
  (3, 1, 8, 1),
  (2, 1, 7, 1),
  (4, 1, 10, 1),
  (5, 1, 11, 1),
  (2, 1, 7, 2),
  (4, 1, 10, 2),
  (5, 1, 11, 2),
  (1, 1, 1, 2),
  (1, 1, 2, 2),
  (3, 1, 8, 2),
  (6, 2, 1, 1),
  (7, 2, 2, 1),
  (8, 2, 3, 1),
  (9, 2, 4, 1),
  (10, 2, 9, 1),
  (2, 2, 10, 1),
  (9, 2, 4, 2),
  (10, 2, 9, 2),
  (2, 2, 10, 2),
  (6, 2, 1, 2),
  (7, 2, 2, 2),
  (8, 2, 3, 2),
  (3, 3, 2, 1),
  (4, 3, 4, 1),
  (5, 3, 5, 1),
  (6, 3, 6, 1),
  (7, 3, 8, 1),
  (8, 3, 9, 1),
  (6, 3, 6, 2),
  (7, 3, 8, 2),
  (8, 3, 9, 2),
  (3, 3, 2, 2),
  (4, 3, 4, 2),
  (5, 3, 5, 2);

-- Donnees de test: evaluations
INSERT INTO evaluation (libelle, type, session, semestre, coefficient, date_session, id_matiere, id_classe, id_enseignant) VALUES
  ('Controle 1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-15', 1, 1, 1),
  ('Examen Final S1', 'EXAMEN_FINAL', 'NORMALE', 1, 2, '2026-01-20', 2, 1, 1),
  ('TP Data Mining', 'TP', 'NORMALE', 2, 1, '2025-11-05', 7, 1, 2),
  ('Rattrapage POO', 'EXAMEN_FINAL', 'RATTRAPAGE', 2, 2, '2026-02-10', 1, 1, 1),
  ('Controle GL', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-18', 3, 2, 8),
  ('Projet Web', 'PROJET', 'NORMALE', 2, 2, '2025-12-01', 4, 2, 9),
  ('Examen Secu', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-18', 9, 2, 10),
  ('CC DevOps', 'CONTROLE_CONTINU', 'NORMALE', 2, 1, '2025-11-20', 10, 2, 2),
  ('CC Reseaux', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-20', 5, 3, 5),
  ('TP Systeme', 'TP', 'NORMALE', 2, 1, '2025-11-28', 6, 3, 6),
  ('Projet Cloud', 'PROJET', 'NORMALE', 1, 2, '2025-12-10', 8, 3, 7),
  ('Examen Secu CCN', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-25', 9, 3, 8);

-- Donnees de test: evaluations supplementaires (6 matieres par semestre)
INSERT INTO evaluation (id_evaluation, libelle, type, session, semestre, coefficient, date_session, id_matiere, id_classe, id_enseignant) VALUES
  (13, 'CC Data Mining S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-22', 7, 1, 2),
  (14, 'TP Cloud S1', 'TP', 'NORMALE', 1, 1, '2025-10-25', 8, 1, 3),
  (15, 'CC DevOps S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-28', 10, 1, 4),
  (16, 'Projet Big Data S1', 'PROJET', 'NORMALE', 1, 2, '2025-11-02', 11, 1, 5),
  (17, 'CC BDD S2', 'CONTROLE_CONTINU', 'NORMALE', 2, 1, '2026-02-15', 2, 1, 1),
  (18, 'TP Cloud S2', 'TP', 'NORMALE', 2, 1, '2026-02-18', 8, 1, 3),
  (19, 'CC DevOps S2', 'CONTROLE_CONTINU', 'NORMALE', 2, 1, '2026-02-20', 10, 1, 4),
  (20, 'Projet Big Data S2', 'PROJET', 'NORMALE', 2, 2, '2026-02-25', 11, 1, 5),
  (21, 'CC POO GLSID S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-16', 1, 2, 6),
  (22, 'CC BDD GLSID S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-19', 2, 2, 7),
  (23, 'Projet Web GLSID S1', 'PROJET', 'NORMALE', 1, 2, '2025-11-03', 4, 2, 9),
  (24, 'CC Secu GLSID S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-11-10', 9, 2, 10),
  (25, 'CC DevOps GLSID S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-11-12', 10, 2, 2),
  (26, 'Examen POO GLSID S2', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-28', 1, 2, 6),
  (27, 'Examen BDD GLSID S2', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-30', 2, 2, 7),
  (28, 'CC GL GLSID S2', 'CONTROLE_CONTINU', 'NORMALE', 2, 1, '2026-02-05', 3, 2, 8),
  (29, 'CC BDD CCN S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-21', 2, 3, 3),
  (30, 'CC Web CCN S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-10-23', 4, 3, 4),
  (31, 'TP Systeme CCN S1', 'TP', 'NORMALE', 1, 1, '2025-11-07', 6, 3, 6),
  (32, 'CC Secu CCN S1', 'CONTROLE_CONTINU', 'NORMALE', 1, 1, '2025-11-15', 9, 3, 8),
  (33, 'Examen BDD CCN S2', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-22', 2, 3, 3),
  (34, 'Projet Web CCN S2', 'PROJET', 'NORMALE', 2, 2, '2026-01-26', 4, 3, 4),
  (35, 'Examen Reseaux CCN S2', 'EXAMEN_FINAL', 'NORMALE', 2, 2, '2026-01-29', 5, 3, 5),
  (36, 'TP Cloud CCN S2', 'TP', 'NORMALE', 2, 1, '2026-02-02', 8, 3, 7);

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
  (4, 4, 15, 1),
  (21, 5, 14, 8),
  (21, 6, 15, 9),
  (21, 7, 13, 10),
  (21, 8, 12, 2),
  (22, 5, 10, 8),
  (22, 6, 11, 9),
  (22, 7, 9, 10),
  (22, 8, 12, 2),
  (23, 5, 16, 8),
  (23, 6, 14, 9),
  (23, 7, 15, 10),
  (23, 8, 13, 2),
  (24, 5, 9, 8),
  (24, 6, 12, 9),
  (24, 7, 10, 10),
  (24, 8, 11, 2),
  (41, 9, 13, 5),
  (41, 10, 12, 6),
  (41, 11, 14, 7),
  (41, 12, 15, 8),
  (42, 9, 11, 5),
  (42, 10, 10, 6),
  (42, 11, 12, 7),
  (42, 12, 9, 8),
  (43, 9, 17, 5),
  (43, 10, 16, 6),
  (43, 11, 15, 7),
  (43, 12, 18, 8),
  (44, 9, 8, 5),
  (44, 10, 9, 6),
  (44, 11, 10, 7),
  (44, 12, 11, 8);

-- Donnees de test: notes supplementaires (6 matieres par semestre)
INSERT INTO note (id_etudiant, id_evaluation, valeur, id_enseignant) VALUES
  (1, 13, 14, 2),
  (1, 14, 15, 3),
  (1, 15, 16, 4),
  (1, 16, 17, 5),
  (1, 17, 13, 1),
  (1, 18, 14, 3),
  (1, 19, 15, 4),
  (1, 20, 16, 5),
  (2, 13, 11, 2),
  (2, 14, 12, 3),
  (2, 15, 13, 4),
  (2, 16, 14, 5),
  (2, 17, 10, 1),
  (2, 18, 11, 3),
  (2, 19, 12, 4),
  (2, 20, 13, 5),
  (3, 13, 9, 2),
  (3, 14, 10, 3),
  (3, 15, 11, 4),
  (3, 16, 12, 5),
  (3, 17, 8, 1),
  (3, 18, 9, 3),
  (3, 19, 10, 4),
  (3, 20, 11, 5),
  (4, 13, 17, 2),
  (4, 14, 16, 3),
  (4, 15, 18, 4),
  (4, 16, 15, 5),
  (4, 17, 16, 1),
  (4, 18, 17, 3),
  (4, 19, 18, 4),
  (4, 20, 16, 5),
  (21, 21, 15, 6),
  (21, 22, 14, 7),
  (21, 23, 16, 9),
  (21, 24, 13, 10),
  (21, 25, 12, 2),
  (21, 26, 14, 6),
  (21, 27, 13, 7),
  (21, 28, 15, 8),
  (22, 21, 10, 6),
  (22, 22, 11, 7),
  (22, 23, 12, 9),
  (22, 24, 9, 10),
  (22, 25, 10, 2),
  (22, 26, 11, 6),
  (22, 27, 12, 7),
  (22, 28, 10, 8),
  (23, 21, 17, 6),
  (23, 22, 16, 7),
  (23, 23, 15, 9),
  (23, 24, 14, 10),
  (23, 25, 13, 2),
  (23, 26, 16, 6),
  (23, 27, 15, 7),
  (23, 28, 14, 8),
  (24, 21, 9, 6),
  (24, 22, 10, 7),
  (24, 23, 11, 9),
  (24, 24, 8, 10),
  (24, 25, 9, 2),
  (24, 26, 10, 6),
  (24, 27, 11, 7),
  (24, 28, 9, 8),
  (41, 29, 13, 3),
  (41, 30, 12, 4),
  (41, 31, 14, 6),
  (41, 32, 15, 8),
  (41, 33, 12, 3),
  (41, 34, 13, 4),
  (41, 35, 14, 5),
  (41, 36, 15, 7),
  (42, 29, 11, 3),
  (42, 30, 10, 4),
  (42, 31, 12, 6),
  (42, 32, 9, 8),
  (42, 33, 10, 3),
  (42, 34, 11, 4),
  (42, 35, 12, 5),
  (42, 36, 10, 7),
  (43, 29, 17, 3),
  (43, 30, 16, 4),
  (43, 31, 18, 6),
  (43, 32, 15, 8),
  (43, 33, 16, 3),
  (43, 34, 17, 4),
  (43, 35, 18, 5),
  (43, 36, 16, 7),
  (44, 29, 8, 3),
  (44, 30, 9, 4),
  (44, 31, 10, 6),
  (44, 32, 11, 8),
  (44, 33, 9, 3),
  (44, 34, 10, 4),
  (44, 35, 11, 5),
  (44, 36, 9, 7);

-- Donnees de test: presence
INSERT INTO presence (id_etudiant, id_matiere, date_absence, statut, source_import) VALUES
  (1, 1, '2025-10-22', 'NON_JUSTIFIEE', 'import_test.csv'),
  (1, 2, '2025-11-12', 'JUSTIFIEE', 'import_test.csv'),
  (2, 1, '2025-12-03', 'NON_JUSTIFIEE', 'import_test.csv'),
  (2, 7, '2025-10-30', 'JUSTIFIEE', 'import_test.csv'),
  (3, 2, '2026-01-08', 'NON_JUSTIFIEE', 'import_test.csv'),
  (3, 7, '2025-11-19', 'JUSTIFIEE', 'import_test.csv');
