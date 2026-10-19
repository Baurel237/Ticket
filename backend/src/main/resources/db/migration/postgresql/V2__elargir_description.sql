-- Migration spécifique PostgreSQL (repertoire vendor, détecté
-- automatiquement par Spring Boot ; les tests H2 ne l'exécutent pas).
-- Les bases créées avant Flyway (par ddl-auto=update) ont une colonne
-- description à 255 caractères : on l'aligne sur le modèle (2000) pour que
-- la validation du schéma passe. Sur une base neuve, colonne déjà à 2000 :
-- l'ALTER est sans effet.
ALTER TABLE tickets ALTER COLUMN description TYPE VARCHAR(2000);
