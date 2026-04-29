package com.resudex.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private final JdbcTemplate db;

    public DatabaseService(JdbcTemplate db) {
        this.db = db;
        initSchema();
    }

    private static String hashPw(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { return raw; }
    }

    private void initSchema() {
        try {
            // alter applications table
            db.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS tech_score INTEGER DEFAULT -1");
            db.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS feedback TEXT");
            db.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS vibes VARCHAR(255)");

            // alter users table
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100)");
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)");
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT");
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(100)");
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_filename VARCHAR(255)");
            db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_text VARCHAR(65535)");
            // migrate CLOB if needed
            try {
                db.execute("ALTER TABLE users ALTER COLUMN resume_text VARCHAR(65535)");
            } catch (Exception ignored) {}

            // job status column
            db.execute("ALTER TABLE jobs ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'OPEN'");

            // create tables
            db.execute("CREATE TABLE IF NOT EXISTS admins (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) UNIQUE, password VARCHAR(100))");
            db.execute("CREATE TABLE IF NOT EXISTS admin_notes (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, note TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            db.execute("CREATE TABLE IF NOT EXISTS notifications (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, message TEXT, is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // resume snapshots
            db.execute("CREATE TABLE IF NOT EXISTS resume_snapshots (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "filename VARCHAR(255), " +
                "java_sc INT DEFAULT 0, " +
                "web_sc INT DEFAULT 0, " +
                "python_sc INT DEFAULT 0, " +
                "cpp_sc INT DEFAULT 0, " +
                "devops_sc INT DEFAULT 0, " +
                "db_sc INT DEFAULT 0, " +
                "exp_yrs INT DEFAULT 0, " +
                "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

            // default admin creds
            List<Map<String, Object>> check = db.queryForList("SELECT * FROM admins");
            if (check.isEmpty()) {
                db.update("INSERT INTO admins (username, password) VALUES (?, ?)",
                    "admin", "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9");
            } else {
                // migrate plain-text admin password
                for (Map<String, Object> adm : check) {
                    String pw = (String) adm.get("password");
                    if (pw != null && pw.length() < 60) {
                        db.update("UPDATE admins SET password = ? WHERE id = ?", hashPw(pw), adm.get("id"));
                    }
                }
            }
            // migrate plain-text user passwords
            try {
                List<Map<String, Object>> users = db.queryForList("SELECT id, password FROM users");
                for (Map<String, Object> u : users) {
                    String pw = (String) u.get("password");
                    if (pw != null && pw.length() < 60) {
                        db.update("UPDATE users SET password = ? WHERE id = ?", hashPw(pw), u.get("id"));
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("DB Fix Log: " + e.getMessage());
        }
    }

    // --- users ---

    public boolean addUser(String usr, String pass, String name, String mail) {
        try {
            db.update("INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)", usr, pass, name, mail);
            return true;
        } catch (Exception ex) {
            return false; // duplicate user
        }
    }

    public Map<String, Object> loginUser(String usr, String pass) {
        try {
            return db.queryForMap("SELECT id AS \"id\", username AS \"username\" FROM users WHERE username = ? AND password = ?", usr, pass);
        } catch (Exception ex) {
            return null;
        }
    }

    public void saveResume(int uid, String file, String text) {
        db.update("UPDATE users SET resume_filename = ?, resume_text = ? WHERE id = ?", file, text, uid);
    }

    public Map<String, Object> getUser(int uid) {
        try {
            return db.queryForMap("SELECT id AS \"id\", username AS \"username\", full_name AS \"full_name\", email AS \"email\", bio AS \"bio\", resume_filename AS \"resume_filename\", resume_text AS \"resume_text\" FROM users WHERE id = ?", uid);
        } catch (Exception ex) {
            return null;
        }
    }

    public void updateProfile(int uid, String name, String mail, String bio) {
        db.update("UPDATE users SET full_name = ?, email = ?, bio = ? WHERE id = ?", name, mail, bio, uid);
    }

    public Map<String, Object> loginAdmin(String usr, String pass) {
        try {
            return db.queryForMap("SELECT id AS \"id\", username AS \"username\" FROM admins WHERE username = ? AND password = ?", usr, pass);
        } catch (Exception ex) {
            return null;
        }
    }

    // --- jobs ---

    public void postJob(String title, String desc, String stat) {
        db.update("INSERT INTO jobs (title, description, status) VALUES (?, ?, ?)", title, desc, stat);
    }

    public List<Map<String, Object>> getJobs(boolean isAdm) {
        String sql = "SELECT id AS \"id\", title AS \"title\", description AS \"description\", status AS \"status\" FROM jobs";
        if (isAdm) {
            return db.queryForList(sql + " ORDER BY id DESC");
        } else {
            return db.queryForList(sql + " WHERE status = 'OPEN' ORDER BY id DESC");
        }
    }

    public Map<String, Object> getJob(int jid) {
        try {
            return db.queryForMap("SELECT id AS \"id\", title AS \"title\", description AS \"description\", status AS \"status\" FROM jobs WHERE id = ?", jid);
        } catch (Exception ex) {
            return null;
        }
    }

    public void editJob(int jid, String title, String desc, String stat) {
        db.update("UPDATE jobs SET title = ?, description = ?, status = ? WHERE id = ?", title, desc, stat, jid);
    }

    public void deleteJob(int jid) {
        db.update("DELETE FROM applications WHERE job_id = ?", jid);
        db.update("DELETE FROM jobs WHERE id = ?", jid);
    }

    // --- applications ---

    public boolean apply(int uid, int jid, int score) {
        try {
            db.update("INSERT INTO applications (user_id, job_id, tech_score) VALUES (?, ?, ?)", uid, jid, score);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<Map<String, Object>> getApps(int jid) {
        String sql = "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
                     "a.vibes AS \"vibes\", " +
                     "u.id AS \"user_id\", u.username AS \"username\", u.resume_text AS \"resume_text\", " +
                     "u.resume_filename AS \"resume_filename\" " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "WHERE a.job_id = ? " +
                     "ORDER BY a.id";
        return db.queryForList(sql, jid);
    }

    public void shortlist(int aid) {
        setStatus(aid, "SELECTED");
    }

    public void setStatus(int aid, String stat) {
        db.update("UPDATE applications SET status = ? WHERE id = ?", stat, aid);
    }

    public List<Map<String, Object>> getUserApps(int uid) {
        String q = "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
                   "a.vibes AS \"vibes\", a.feedback AS \"feedback\", j.id AS \"job_id\", j.title AS \"title\", j.description AS \"description\" " +
                   "FROM applications a " +
                   "JOIN jobs j ON a.job_id = j.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY a.id DESC";
        return db.queryForList(q, uid);
    }

    public void setVibes(int aid, String tags) {
        db.update("UPDATE applications SET vibes = ? WHERE id = ?", tags, aid);
    }

    // --- notifications and feedback ---

    public void notify(int uid, String msg) {
        db.update("INSERT INTO notifications (user_id, message) VALUES (?, ?)", uid, msg);
    }

    public List<Map<String, Object>> getUnread(int uid) {
        return db.queryForList("SELECT id AS \"id\", user_id AS \"user_id\", message AS \"message\", is_read AS \"is_read\", created_at AS \"created_at\" FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC", uid);
    }

    public void markRead(int nid) {
        db.update("UPDATE notifications SET is_read = TRUE WHERE id = ?", nid);
    }

    public void setFeedback(int aid, String fbk) {
        db.update("UPDATE applications SET feedback = ? WHERE id = ?", fbk, aid);
    }

    public int getUid(int aid) {
        try {
            return db.queryForObject("SELECT user_id FROM applications WHERE id = ?", Integer.class, aid);
        } catch (Exception ex) {
            return -1;
        }
    }

    public void saveToken(String usr, String tok) {
        db.update("UPDATE users SET reset_token = ? WHERE username = ?", tok, usr);
    }

    public boolean resetPw(String tok, String pass) {
        int rows = db.update("UPDATE users SET password = ?, reset_token = NULL WHERE reset_token = ?", pass, tok);
        return rows > 0;
    }

    // --- trajectory ---

    public void saveSnapshot(int uid, String filename, int javaSc, int webSc, int pySc, int cppSc, int devopsSc, int dbSc, int exp) {
        db.update(
            "INSERT INTO resume_snapshots (user_id, filename, java_sc, web_sc, python_sc, cpp_sc, devops_sc, db_sc, exp_yrs) VALUES (?,?,?,?,?,?,?,?,?)",
            uid, filename, javaSc, webSc, pySc, cppSc, devopsSc, dbSc, exp
        );
    }

    public List<Map<String, Object>> getSnapshots(int uid) {
        return db.queryForList(
            "SELECT id AS \"id\", filename AS \"filename\", java_sc AS \"java_sc\", web_sc AS \"web_sc\", " +
            "python_sc AS \"python_sc\", cpp_sc AS \"cpp_sc\", devops_sc AS \"devops_sc\", db_sc AS \"db_sc\", " +
            "exp_yrs AS \"exp_yrs\", uploaded_at AS \"uploaded_at\" " +
            "FROM resume_snapshots WHERE user_id = ? ORDER BY uploaded_at ASC", uid
        );
    }

    // --- battle ---

    public List<Map<String, Object>> getUsersWithResume() {
        return db.queryForList(
            "SELECT id AS \"id\", username AS \"username\", full_name AS \"full_name\", " +
            "resume_text AS \"resume_text\", resume_filename AS \"resume_filename\" " +
            "FROM users WHERE resume_text IS NOT NULL AND resume_text != ''"
        );
    }

    // --- notes ---

    public void addNote(int uid, String note) {
        db.update("INSERT INTO admin_notes (user_id, note) VALUES (?, ?)", uid, note);
    }

    public List<Map<String, Object>> getNotes(int uid) {
        return db.queryForList("SELECT id AS \"id\", user_id AS \"user_id\", note AS \"note\", created_at AS \"created_at\" FROM admin_notes WHERE user_id = ? ORDER BY created_at DESC", uid);
    }
}
