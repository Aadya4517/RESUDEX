package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * ApiClient - handles all HTTP calls to the Spring Boot backend.
 */
public class ApiClient {

    public static final String BASE = "http://localhost:8080/api";

    public static String register(String username, String password, String fullName, String email) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", username);
            body.addProperty("password", password);
            body.addProperty("fullName", fullName);
            body.addProperty("email",    email);
            
            String resp = postJson("/users/register", body.toString());
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Connection failed.";
        }
    }

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

    public static boolean adminLogin(String username, String password) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
            String resp = postJson("/auth/admin/login", body);
            return !JsonParser.parseString(resp).getAsJsonObject().has("error");
        } catch (Exception e) {
            return false;
        }
    }

    public static JsonArray getJobs() {
        try {
            return JsonParser.parseString(get("/jobs")).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static JsonArray getMatchedJobs(int userId) {
        try {
            return JsonParser.parseString(get("/jobs/matched/" + userId)).getAsJsonArray();
        } catch (Exception e) {
            return getJobs();
        }
    }

    public static String createJob(String title, String description) {
        try {
            String body = "{\"title\":\"" + esc(title) + "\",\"description\":\"" + esc(description) + "\"}";
            String resp = postJson("/jobs", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Failed.";
        }
    }

    public static String updateJob(int jobId, String title, String description) {
        try {
            String body = "{\"title\":\"" + esc(title) + "\",\"description\":\"" + esc(description) + "\"}";
            String resp = putJson("/jobs/" + jobId, body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Update failed.";
        }
    }

    public static String deleteJob(int jobId) {
        try {
            String resp = delete("/jobs/" + jobId);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Deletion failed.";
        }
    }

    public static String uploadResume(int userId, File file) {
        try {
            String boundary = "FormBoundary" + System.currentTimeMillis();
            URL url = new URL(BASE + "/resume/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"userId\"\r\n\r\n");
                out.writeBytes(userId + "\r\n");
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n\r\n");
                Files.copy(file.toPath(), out);
                out.writeBytes("\r\n--" + boundary + "--\r\n");
                out.flush();
            }

            String resp = readResponse(conn);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String generateCoverLetter(int userId, int jobId) {
        try {
            String body = "{\"userId\":" + userId + "}";
            String resp = postJson("/jobs/" + jobId + "/cover-letter", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("letter")) return obj.get("letter").getAsString();
            return "Failed to generate cover letter.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String applyForJob(int userId, int jobId, int techScore) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("userId", userId);
            body.addProperty("techScore", techScore);
            String resp = postJson("/jobs/" + jobId + "/apply", body.toString());
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Application failed.";
        }
    }

    public static JsonObject getAdminAnalytics() {
        try {
            String resp = get("/admin/analytics");
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getUserApplications(int userId) {
        try {
            return JsonParser.parseString(get("/jobs/applications/" + userId)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static JsonArray getApplicants(int jobId) {
        try {
            return JsonParser.parseString(get("/jobs/" + jobId + "/applicants")).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static String selectApplicant(int applicationId) {
        try {
            String resp = postJson("/applicants/" + applicationId + "/select", "{}");
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Selection failed.";
        }
    }

    public static JsonObject getResumeAnalytics(int userId) {
        try {
            String resp = get("/users/" + userId + "/resume/analytics");
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonObject getUserProfile(int userId) {
        try {
            String resp = get("/users/" + userId);
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String updateUserProfile(int userId, String fullName, String email, String bio) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("full_name", fullName);
            body.addProperty("email",     email);
            body.addProperty("bio",       bio);
            String resp = putJson("/users/" + userId, body.toString());
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Profile update failed.";
        }
    }

    public static JsonArray getNotifications(int userId) {
        try {
            return JsonParser.parseString(get("/notifications/" + userId)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static void markNotificationRead(int id) {
        try { postJson("/notifications/read/" + id, "{}"); } catch (Exception ignored) {}
    }

    public static String provideFeedback(int appId, String feedback) {
        try {
            String body = "{\"feedback\":\"" + esc(feedback) + "\"}";
            String resp = postJson("/applications/" + appId + "/feedback", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Failed to send feedback.";
        }
    }

    public static String forgotPassword(String username) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\"}";
            String resp = postJson("/auth/forgot-password", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Request failed.";
        }
    }

    public static String resetPassword(String token, String newPassword) {
        try {
            String body = "{\"token\":\"" + esc(token) + "\",\"password\":\"" + esc(newPassword) + "\"}";
            String resp = postJson("/auth/reset-password", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return obj.get("error").getAsString();
            return null;
        } catch (Exception e) {
            return "Reset failed.";
        }
    }

    public static int socialLogin(String provider, String email) {
        try {
            String body = "{\"provider\":\"" + provider + "\",\"email\":\"" + email + "\"}";
            String resp = postJson("/auth/social-login", body);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (obj.has("error")) return -1;
            ResudexApp.currentUsername = obj.get("username").getAsString();
            return obj.get("userId").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }

    private static String postJson(String endpoint, String jsonBody) throws Exception {
        return sendJson(endpoint, "POST", jsonBody);
    }

    private static String putJson(String endpoint, String jsonBody) throws Exception {
        return sendJson(endpoint, "PUT", jsonBody);
    }

    private static String sendJson(String endpoint, String method, String jsonBody) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(jsonBody.getBytes("UTF-8")); }
        return readResponse(conn);
    }

    private static String get(String endpoint) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return readResponse(conn);
    }

    private static String delete(String endpoint) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        InputStream stream = null;
        try { stream = conn.getInputStream(); } catch (IOException e) { stream = conn.getErrorStream(); }
        if (stream == null) return "{}";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
