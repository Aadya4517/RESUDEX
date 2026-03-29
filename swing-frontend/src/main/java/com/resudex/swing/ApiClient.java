package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * ApiClient - handles all HTTP calls to the Spring Boot backend.
 * Uses plain HttpURLConnection (no extra library needed beyond Gson).
 */
public class ApiClient {

    public static final String BASE = "http://localhost:8081/api";

    // ===================== AUTH =====================

    /**
     * Register a new user. Returns null on success, error message on failure.
     */
    public static String register(String username, String password) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
            String resp = postJson("/auth/register", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null; // success
        } catch (Exception e) {
            return "Cannot connect to backend. Is the server running?";
        }
    }

    /**
     * Login a user. Returns userId (>= 1) on success, -1 on failure.
     * Sets ResudexApp.currentUsername as a side effect.
     */
    public static int login(String username, String password) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
            String resp = postJson("/auth/login", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return -1;
            ResudexApp.currentUsername = obj.get("username").getAsString();
            return obj.get("userId").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Admin login. Returns true if credentials correct.
     */
    public static boolean adminLogin(String username, String password) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
            String resp = postJson("/auth/admin/login", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            return !obj.has("error");
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== JOBS =====================

    /**
     * Get all jobs. Returns JsonArray of {id, title, description}.
     */
    public static JsonArray getJobs() {
        try {
            String resp = get("/jobs");
            return JsonParser.parseString(resp).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    /**
     * Get jobs matched for a specific user.
     */
    public static JsonArray getMatchedJobs(int userId) {
        try {
            String resp = get("/jobs/matched/" + userId);
            return JsonParser.parseString(resp).getAsJsonArray();
        } catch (Exception e) {
            return getJobs(); // Fallback to all jobs
        }
    }

    /**
     * Admin: create a new job. Returns null on success, error string on failure.
     */
    public static String createJob(String title, String description) {
        try {
            String body = "{\"title\":\"" + esc(title) + "\",\"description\":\"" + esc(description) + "\"}";
            String resp = postJson("/jobs", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Cannot connect to backend.";
        }
    }

    // ===================== RESUME =====================

    /**
     * Upload resume file for a user. Returns null on success, error string on failure.
     */
    public static String uploadResume(int userId, File file) {
        try {
            String boundary = "FormBoundary" + System.currentTimeMillis();
            URL url = new URL(BASE + "/resume/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                // userId part
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"userId\"\r\n\r\n");
                out.writeBytes(userId + "\r\n");

                // file part
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
                out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                Files.copy(file.toPath(), out);
                out.writeBytes("\r\n--" + boundary + "--\r\n");
                out.flush();
            }

            String resp = readResponse(conn);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null; // success

        } catch (Exception e) {
            return "Upload failed: " + e.getMessage();
        }
    }

    // ===================== APPLICATIONS =====================

    /**
     * User applies for a job. Returns null on success, error string on failure.
     */
    public static String applyForJob(int userId, int jobId) {
        try {
            String body = "{\"userId\":" + userId + "}";
            String resp = postJson("/jobs/" + jobId + "/apply", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Cannot connect to backend.";
        }
    }

    /**
     * User: get applied jobs. Returns JsonArray.
     */
    public static JsonArray getUserApplications(int userId) {
        try {
            String resp = get("/jobs/applications/" + userId);
            return JsonParser.parseString(resp).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    /**
     * Admin: get ranked applicants for a job. Returns JsonArray.
     */
    public static JsonArray getApplicants(int jobId) {
        try {
            String resp = get("/jobs/" + jobId + "/applicants");
            return JsonParser.parseString(resp).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    /**
     * Admin: select / shortlist a candidate by application ID.
     */
    public static String selectApplicant(int applicationId) {
        try {
            String resp = postJson("/applicants/" + applicationId + "/select", "{}");
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Cannot connect to backend.";
        }
    }

    // ===================== HELPERS =====================

    private static String postJson(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(15_000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }

        return readResponse(conn);
    }

    private static String get(String endpoint) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(15_000);
        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        InputStream stream;
        try {
            stream = conn.getInputStream();
        } catch (IOException e) {
            stream = conn.getErrorStream();
        }
        if (stream == null) return "{}";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    /** Escape special chars for simple JSON string building */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
