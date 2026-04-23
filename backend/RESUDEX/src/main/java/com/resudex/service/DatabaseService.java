package com.resudex.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * DB Core Service. 
 * Handles all the low-level SQL stuff using Spring's Template.
 */
@Service
public class DatabaseService {

    private final JdbcTemplate db_tpl;

    public DatabaseService(JdbcTemplate db_tpl) {
        this.db_tpl = db_tpl;
        init_db_schema();
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

    private void init_db_schema() {
        try {
            // we need these columns for the new features
            db_tpl.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS tech_score INTEGER DEFAULT -1");
            db_tpl.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS feedback TEXT");
            db_tpl.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS vibes VARCHAR(255)");
            
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100)");
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)");
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT");
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(100)");
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_filename VARCHAR(255)");
            db_tpl.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_text VARCHAR(65535)");
            // migrate existing CLOB column to VARCHAR if needed
            try {
                db_tpl.execute("ALTER TABLE users ALTER COLUMN resume_text VARCHAR(65535)");
            } catch (Exception ignored) {}

            // track if job is open or closed
            db_tpl.execute("ALTER TABLE jobs ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'OPEN'");

            // basic auth tables
            db_tpl.execute("CREATE TABLE IF NOT EXISTS admins (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) UNIQUE, password VARCHAR(100))");
            db_tpl.execute("CREATE TABLE IF NOT EXISTS admin_notes (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, note TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            db_tpl.execute("CREATE TABLE IF NOT EXISTS notifications (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, message TEXT, is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // resume snapshot history for skill trajectory
            db_tpl.execute("CREATE TABLE IF NOT EXISTS resume_snapshots (" +
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

            // default creds (password is SHA-256 of "admin123")
            List<Map<String, Object>> check = db_tpl.queryForList("SELECT * FROM admins");
            if (check.isEmpty()) {
                db_tpl.update("INSERT INTO admins (username, password) VALUES (?, ?)",
                    "admin", "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9");
            } else {
                // migrate plain-text admin password if not yet hashed (length < 60 means not hashed)
                for (Map<String, Object> adm : check) {
                    String pw = (String) adm.get("password");
                    if (pw != null && pw.length() < 60) {
                        String hashed = hashPw(pw);
                        db_tpl.update("UPDATE admins SET password = ? WHERE id = ?", hashed, adm.get("id"));
                    }
                }
            }
            // migrate plain-text user passwords
            try {
                List<Map<String, Object>> users = db_tpl.queryForList("SELECT id, password FROM users");
                for (Map<String, Object> u : users) {
                    String pw = (String) u.get("password");
                    if (pw != null && pw.length() < 60) {
                        db_tpl.update("UPDATE users SET password = ? WHERE id = ?", hashPw(pw), u.get("id"));
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("DB Fix Log: " + e.getMessage());
        }
    }

    // --- USER STUFF ---

    public boolean add_new_user(String usr, String pass, String name, String mail) {
        try {
            db_tpl.update("INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)", usr, pass, name, mail);
            return true;
        } catch (Exception ex) {
            return false; // likely duplicate user
        }
    }

    public Map<String, Object> auth_usr(String usr, String pass) {
        try {
            return db_tpl.queryForMap("SELECT id AS \"id\", username AS \"username\" FROM users WHERE username = ? AND password = ?", usr, pass);
        } catch (Exception ex) {
            return null;
        }
    }

    public void store_cv_text(int uid, String file, String raw_text) {
        db_tpl.update("UPDATE users SET resume_filename = ?, resume_text = ? WHERE id = ?", file, raw_text, uid);
    }

    public Map<String, Object> get_usr_by_id(int uid) {
        try {
            return db_tpl.queryForMap("SELECT id AS \"id\", username AS \"username\", full_name AS \"full_name\", email AS \"email\", bio AS \"bio\", resume_filename AS \"resume_filename\", resume_text AS \"resume_text\" FROM users WHERE id = ?", uid);
        } catch (Exception ex) {
            return null;
        }
    }

    public void update_profile(int uid, String name, String mail, String bio_text) {
        db_tpl.update("UPDATE users SET full_name = ?, email = ?, bio = ? WHERE id = ?", name, mail, bio_text, uid);
    }

    public Map<String, Object> auth_admin(String usr, String pass) {
        try {
            return db_tpl.queryForMap("SELECT id AS \"id\", username AS \"username\" FROM admins WHERE username = ? AND password = ?", usr, pass);
        } catch (Exception ex) {
            return null;
        }
    }

    // --- JOBS ---

    public void post_job(String title, String desc, String stat) {
        db_tpl.update("INSERT INTO jobs (title, description, status) VALUES (?, ?, ?)", title, desc, stat);
    }

    public List<Map<String, Object>> list_all_jobs(boolean is_adm) {
        String sql = "SELECT id AS \"id\", title AS \"title\", description AS \"description\", status AS \"status\" FROM jobs";
        if (is_adm) {
            return db_tpl.queryForList(sql + " ORDER BY id DESC");
        } else {
            return db_tpl.queryForList(sql + " WHERE status = 'OPEN' ORDER BY id DESC");
        }
    }

    public Map<String, Object> get_job_info(int jid) {
        try {
            return db_tpl.queryForMap("SELECT id AS \"id\", title AS \"title\", description AS \"description\", status AS \"status\" FROM jobs WHERE id = ?", jid);
        } catch (Exception ex) {
            return null;
        }
    }

    public void edit_job_data(int jid, String title, String desc, String stat) {
        db_tpl.update("UPDATE jobs SET title = ?, description = ?, status = ? WHERE id = ?", title, desc, stat, jid);
    }

    public void kill_job(int jid) {
        db_tpl.update("DELETE FROM applications WHERE job_id = ?", jid);
        db_tpl.update("DELETE FROM jobs WHERE id = ?", jid);
    }

    // --- APPS ---

    public boolean sub_app(int uid, int jid, int score) {
        try {
            db_tpl.update("INSERT INTO applications (user_id, job_id, tech_score) VALUES (?, ?, ?)", uid, jid, score);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<Map<String, Object>> list_apps_for_job(int jid) {
        String sql = "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
                     "a.vibes AS \"vibes\", " +
                     "u.id AS \"user_id\", u.username AS \"username\", u.resume_text AS \"resume_text\", " +
                     "u.resume_filename AS \"resume_filename\" " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "WHERE a.job_id = ? " +
                     "ORDER BY a.id";
        return db_tpl.queryForList(sql, jid);
    }

    public void pick_applicant(int aid) {
        change_app_status(aid, "SELECTED");
    }

    public void change_app_status(int aid, String stat) {
        db_tpl.update("UPDATE applications SET status = ? WHERE id = ?", stat, aid);
    }

    public List<Map<String, Object>> usr_apps(int uid) {
        String q = "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
                   "a.vibes AS \"vibes\", a.feedback AS \"feedback\", j.id AS \"job_id\", j.title AS \"title\", j.description AS \"description\" " +
                   "FROM applications a " +
                   "JOIN jobs j ON a.job_id = j.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY a.id DESC";
        return db_tpl.queryForList(q, uid);
    }

    public void set_app_vibes(int aid, String tag_str) {
        db_tpl.update("UPDATE applications SET vibes = ? WHERE id = ?", tag_str, aid);
    }

    // --- NOTIFS & FEEDBACK ---

    public void push_notif(int uid, String msg) {
        db_tpl.update("INSERT INTO notifications (user_id, message) VALUES (?, ?)", uid, msg);
    }

    public List<Map<String, Object>> get_unread(int uid) {
        return db_tpl.queryForList("SELECT id AS \"id\", user_id AS \"user_id\", message AS \"message\", is_read AS \"is_read\", created_at AS \"created_at\" FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC", uid);
    }

    public void seen_notif(int nid) {
        db_tpl.update("UPDATE notifications SET is_read = TRUE WHERE id = ?", nid);
    }

    public void give_feedback(int aid, String fbk) {
        db_tpl.update("UPDATE applications SET feedback = ? WHERE id = ?", fbk, aid);
    }

    public int find_uid(int aid) {
        try {
            return db_tpl.queryForObject("SELECT user_id FROM applications WHERE id = ?", Integer.class, aid);
        } catch (Exception ex) {
            return -1;
        }
    }

    public void save_reset_token(String usr, String tok) {
        db_tpl.update("UPDATE users SET reset_token = ? WHERE username = ?", tok, usr);
    }

    public boolean do_pw_reset(String tok, String pass) {
        int rows = db_tpl.update("UPDATE users SET password = ?, reset_token = NULL WHERE reset_token = ?", pass, tok);
        return rows > 0;
    }

    // --- TRAJECTORY ---

    public void save_snapshot(int uid, String fname, int java_sc, int web_sc, int py_sc, int cpp_sc, int devops_sc, int db_sc, int exp) {
        db_tpl.update(
            "INSERT INTO resume_snapshots (user_id, filename, java_sc, web_sc, python_sc, cpp_sc, devops_sc, db_sc, exp_yrs) VALUES (?,?,?,?,?,?,?,?,?)",
            uid, fname, java_sc, web_sc, py_sc, cpp_sc, devops_sc, db_sc, exp
        );
    }

    public List<Map<String, Object>> get_snapshots(int uid) {
        return db_tpl.queryForList(
            "SELECT id AS \"id\", filename AS \"filename\", java_sc AS \"java_sc\", web_sc AS \"web_sc\", " +
            "python_sc AS \"python_sc\", cpp_sc AS \"cpp_sc\", devops_sc AS \"devops_sc\", db_sc AS \"db_sc\", " +
            "exp_yrs AS \"exp_yrs\", uploaded_at AS \"uploaded_at\" " +
            "FROM resume_snapshots WHERE user_id = ? ORDER BY uploaded_at ASC", uid
        );
    }

    // --- BATTLE ---

    public List<Map<String, Object>> get_all_users_with_resume() {
        return db_tpl.queryForList(
            "SELECT id AS \"id\", username AS \"username\", full_name AS \"full_name\", " +
            "resume_text AS \"resume_text\", resume_filename AS \"resume_filename\" " +
            "FROM users WHERE resume_text IS NOT NULL AND resume_text != ''"
        );
    }

    // --- NOTES ---

    public void add_note(int uid, String raw_note) {        db_tpl.update("INSERT INTO admin_notes (user_id, note) VALUES (?, ?)", uid, raw_note);
    }

    public List<Map<String, Object>> get_notes(int uid) {
        return db_tpl.queryForList("SELECT id AS \"id\", user_id AS \"user_id\", note AS \"note\", created_at AS \"created_at\" FROM admin_notes WHERE user_id = ? ORDER BY created_at DESC", uid);
    }
}
