package com.resudex.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * DatabaseService - handles all JDBC operations for RESUDEX.
 * Uses Spring's JdbcTemplate for simple, readable queries.
 */
@Service
public class DatabaseService {

    private final JdbcTemplate jdbc;

    public DatabaseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        ensureSchema();
    }

    private void ensureSchema() {
        try {
            // H2-specific "ALTER TABLE ... ADD COLUMN IF NOT EXISTS"
            jdbc.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS tech_score INTEGER DEFAULT -1");
            jdbc.execute("ALTER TABLE applications ADD COLUMN IF NOT EXISTS feedback TEXT");
            
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100)");
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)");
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT");
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(100)");
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_filename VARCHAR(255)");
            jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_text CLOB");

            // Create notifications table
            jdbc.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT, " +
                         "message TEXT, " +
                         "is_read BOOLEAN DEFAULT FALSE, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) {
            System.err.println("Migration notice: " + e.getMessage());
        }
    }

    // ===================== USER METHODS =====================

    /**
     * Register a new user with full details. Returns false if username already taken.
     */
    public boolean registerUser(String username, String password, String fullName, String email) {
        try {
            jdbc.update(
                "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)",
                username, password, fullName, email
            );
            return true;
        } catch (Exception e) {
            return false; // duplicate username
        }
    }

    /**
     * Login user. Returns user map {id, username} or null on failure.
     */
    public Map<String, Object> loginUser(String username, String password) {
        try {
            return jdbc.queryForMap(
                "SELECT id AS \"id\", username AS \"username\" FROM users WHERE username = ? AND password = ?",
                username, password
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Save uploaded resume text for a user.
     */
    public void saveResume(int userId, String filename, String text) {
        jdbc.update(
            "UPDATE users SET resume_filename = ?, resume_text = ? WHERE id = ?",
            filename, text, userId
        );
    }

    /**
     * Get user by ID.
     */
    public Map<String, Object> getUserById(int userId) {
        try {
            return jdbc.queryForMap(
                "SELECT id AS \"id\", username AS \"username\", full_name AS \"full_name\", " +
                "email AS \"email\", bio AS \"bio\", resume_filename AS \"resume_filename\", " +
                "resume_text AS \"resume_text\" FROM users WHERE id = ?",
                userId
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update user profile details.
     */
    public void updateUserProfile(int userId, String fullName, String email, String bio) {
        jdbc.update(
            "UPDATE users SET full_name = ?, email = ?, bio = ? WHERE id = ?",
            fullName, email, bio, userId
        );
    }

    // ===================== JOB METHODS =====================

    /**
     * Create a new job posting.
     */
    public void createJob(String title, String description) {
        jdbc.update(
            "INSERT INTO jobs (title, description) VALUES (?, ?)",
            title, description
        );
    }

    /**
     * Get all jobs.
     */
    public List<Map<String, Object>> getAllJobs() {
        return jdbc.queryForList("SELECT * FROM jobs ORDER BY id");
    }

    /**
     * Get a single job by ID.
     */
    public Map<String, Object> getJobById(int jobId) {
        try {
            return jdbc.queryForMap("SELECT id AS \"id\", title AS \"title\", description AS \"description\" FROM jobs WHERE id = ?", jobId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update a job posting.
     */
    public void updateJob(int id, String title, String description) {
        jdbc.update(
            "UPDATE jobs SET title = ?, description = ? WHERE id = ?",
            title, description, id
        );
    }

    /**
     * Delete a job posting and its applications.
     */
    public void deleteJob(int id) {
        // Cascading manually if not set in DB
        jdbc.update("DELETE FROM applications WHERE job_id = ?", id);
        jdbc.update("DELETE FROM jobs WHERE id = ?", id);
    }

    // ===================== APPLICATION METHODS =====================

    /**
     * Apply for a job. Returns false if already applied.
     */
    public boolean applyForJob(int userId, int jobId, int techScore) {
        try {
            jdbc.update(
                "INSERT INTO applications (user_id, job_id, tech_score) VALUES (?, ?, ?)",
                userId, jobId, techScore
            );
            return true;
        } catch (Exception e) {
            return false; // already applied or error
        }
    }

    /**
     * Get all applicants for a job, joining user info.
     */
    public List<Map<String, Object>> getApplicantsForJob(int jobId) {
        return jdbc.queryForList(
            "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
            "u.id AS \"user_id\", u.username AS \"username\", u.resume_text AS \"resume_text\", " +
            "u.resume_filename AS \"resume_filename\" " +
            "FROM applications a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.job_id = ? " +
            "ORDER BY a.id",
            jobId
        );
    }

    /**
     * Mark an applicant as SELECTED. (Legacy)
     */
    public void selectApplicant(int applicationId) {
        updateApplicationStatus(applicationId, "SELECTED");
    }

    /**
     * Update application status generically (for Kanban board).
     */
    public void updateApplicationStatus(int applicationId, String status) {
        jdbc.update(
            "UPDATE applications SET status = ? WHERE id = ?",
            status, applicationId
        );
    }

    /**
     * Get all applications for a specific user, joining job info.
     */
    public List<Map<String, Object>> getUserApplications(int userId) {
        return jdbc.queryForList(
            "SELECT a.id AS \"app_id\", a.status AS \"status\", a.tech_score AS \"tech_score\", " +
            "a.feedback AS \"feedback\", j.id AS \"job_id\", j.title AS \"title\", j.description AS \"description\" " +
            "FROM applications a " +
            "JOIN jobs j ON a.job_id = j.id " +
            "WHERE a.user_id = ? " +
            "ORDER BY a.id DESC",
            userId
        );
    }

    // ===================== NOTIFICATION & FEEDBACK METHODS =====================

    public void addNotification(int userId, String message) {
        jdbc.update("INSERT INTO notifications (user_id, message) VALUES (?, ?)", userId, message);
    }

    public List<Map<String, Object>> getUnreadNotifications(int userId) {
        return jdbc.queryForList(
            "SELECT id AS \"id\", user_id AS \"user_id\", message AS \"message\", " +
            "is_read AS \"is_read\", created_at AS \"created_at\" " +
            "FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC",
            userId
        );
    }

    public void markNotificationRead(int notificationId) {
        jdbc.update("UPDATE notifications SET is_read = TRUE WHERE id = ?", notificationId);
    }

    public void updateApplicationFeedback(int applicationId, String feedback) {
        jdbc.update("UPDATE applications SET feedback = ? WHERE id = ?", feedback, applicationId);
    }

    public int getApplicationUserId(int applicationId) {
        try {
            return jdbc.queryForObject("SELECT user_id FROM applications WHERE id = ?", Integer.class, applicationId);
        } catch (Exception e) {
            return -1;
        }
    }

    public void setUserResetToken(String username, String token) {
        jdbc.update("UPDATE users SET reset_token = ? WHERE username = ?", token, username);
    }

    public boolean resetPassword(String token, String newPassword) {
        int updated = jdbc.update("UPDATE users SET password = ?, reset_token = NULL WHERE reset_token = ?", newPassword, token);
        return updated > 0;
    }

    // ===================== ADMIN NOTES METHODS =====================

    public void addAdminNote(int userId, String note) {
        jdbc.update("INSERT INTO admin_notes (user_id, note) VALUES (?, ?)", userId, note);
    }

    public List<Map<String, Object>> getAdminNotesForUser(int userId) {
        return jdbc.queryForList(
            "SELECT id AS \"id\", user_id AS \"user_id\", note AS \"note\", created_at AS \"created_at\" " +
            "FROM admin_notes WHERE user_id = ? ORDER BY created_at DESC", 
            userId
        );
    }
}
