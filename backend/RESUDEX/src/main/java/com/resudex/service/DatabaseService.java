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
    }

    // ===================== USER METHODS =====================

    /**
     * Register a new user. Returns false if username already taken.
     */
    public boolean registerUser(String username, String password) {
        try {
            jdbc.update(
                "INSERT INTO users (username, password) VALUES (?, ?)",
                username, password
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
                "SELECT id, username FROM users WHERE username = ? AND password = ?",
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
                "SELECT id, username, resume_filename, resume_text FROM users WHERE id = ?",
                userId
            );
        } catch (Exception e) {
            return null;
        }
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
            return jdbc.queryForMap("SELECT * FROM jobs WHERE id = ?", jobId);
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== APPLICATION METHODS =====================

    /**
     * Apply for a job. Returns false if already applied.
     */
    public boolean applyForJob(int userId, int jobId) {
        try {
            jdbc.update(
                "INSERT INTO applications (user_id, job_id) VALUES (?, ?)",
                userId, jobId
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
            "SELECT a.id AS app_id, a.status, u.id AS user_id, " +
            "u.username, u.resume_text, u.resume_filename " +
            "FROM applications a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.job_id = ? " +
            "ORDER BY a.id",
            jobId
        );
    }

    /**
     * Mark an applicant as SELECTED.
     */
    public void selectApplicant(int applicationId) {
        jdbc.update(
            "UPDATE applications SET status = 'SELECTED' WHERE id = ?",
            applicationId
        );
    }

    /**
     * Get all applications for a specific user, joining job info.
     */
    public List<Map<String, Object>> getUserApplications(int userId) {
        return jdbc.queryForList(
            "SELECT a.id AS app_id, a.status, j.id AS job_id, " +
            "j.title, j.description " +
            "FROM applications a " +
            "JOIN jobs j ON a.job_id = j.id " +
            "WHERE a.user_id = ? " +
            "ORDER BY a.id DESC",
            userId
        );
    }
}
