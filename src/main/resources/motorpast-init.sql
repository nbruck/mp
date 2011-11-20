CREATE DATABASE motorpast_scheme;

CREATE USER 'mpSimpleUser'@'localhost'
    IDENTIFIED BY '4793cD1941e73D453df32bef15';

GRANT
    SELECT, INSERT, UPDATE, CREATE
    ON
    motorpast_scheme.*
    TO
    mpSimpleUser@'localhost'
    IDENTIFIED BY '4793cD1941e73D453df32bef15';

-- after creating tables remove the create-privileg
REVOKE
    CREATE
    ON
    motorpast_scheme.*
    FROM
    mpSimpleUser@'localhost';
-- and don't forget
FLUSH PRIVILEGES;