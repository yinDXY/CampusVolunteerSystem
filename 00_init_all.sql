SET NAMES utf8mb4;

DROP DATABASE IF EXISTS volunteer_db;

SOURCE database/01_create_tables.sql;
SOURCE database/02_triggers.sql;
SOURCE database/03_procedures.sql;
SOURCE database/04_views.sql;
SOURCE database/05_indexes.sql;
SOURCE database_init/init_data.sql;
