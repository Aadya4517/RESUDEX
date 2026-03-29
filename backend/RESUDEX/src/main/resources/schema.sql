-- Users table
CREATE TABLE IF NOT EXISTS users (
    id         INTEGER PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(100) NOT NULL,
    resume_filename VARCHAR(255),
    resume_text    CLOB
);

-- Jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id          INTEGER PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    description CLOB NOT NULL
);

-- Applications table (one per user per job)
CREATE TABLE IF NOT EXISTS applications (
    id       INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id  INTEGER NOT NULL,
    job_id   INTEGER NOT NULL,
    status   VARCHAR(50) DEFAULT 'APPLIED',
    UNIQUE (user_id, job_id)
);
