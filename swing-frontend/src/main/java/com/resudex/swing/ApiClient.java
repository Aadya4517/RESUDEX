package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * Net client for the app.
 * 200% Humanized.
 */
public class ApiClient {

    public static final String BASE = "http://localhost:8080/api";

    // reg a new guy
    public static String add_usr(String u, String p, String name, String mail) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("username", u);
            data.addProperty("password", p);
            data.addProperty("f_name",   name);
            data.addProperty("email",    mail);
            
            String r = post_json("/auth/register_usr", data.toString());
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Conn failed.";
        }
    }

    // login logic
    public static int do_login(String u, String p) {
        try {
            String b = "{\"username\":\"" + esc(u) + "\",\"password\":\"" + esc(p) + "\"}";
            String r = post_json("/auth/log_in", b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return -1;
            ResudexApp.usr = obj.get("usr").getAsString();
            return obj.get("uid").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }

    // admin check
    public static boolean admin_auth(String u, String p) {
        try {
            String b = "{\"username\":\"" + esc(u) + "\",\"password\":\"" + esc(p) + "\"}";
            String r = post_json("/auth/admin_log_in", b);
            return !JsonParser.parseString(r).getAsJsonObject().has("err");
        } catch (Exception e) {
            return false;
        }
    }

    public static JsonArray list_all_jobs(boolean is_adm) {
        try {
            return JsonParser.parseString(get("/jobs/list?is_adm=" + is_adm)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static JsonArray list_all_jobs() {
        return list_all_jobs(false);
    }

    public static JsonArray list_match_jobs(int uid) {
        try {
            return JsonParser.parseString(get("/jobs/matched_for/" + uid)).getAsJsonArray();
        } catch (Exception e) {
            return list_all_jobs();
        }
    }

    public static String save_new_job(String t, String d, String s) {
        try {
            String b = "{\"title\":\"" + esc(t) + "\", \"description\":\"" + esc(d) + "\", \"status\":\"" + esc(s) + "\"}";
            String r = post_json("/jobs/create", b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Save failed.";
        }
    }
 
    public static String edit_job(int jid, String t, String d, String s) {
        try {
            String b = "{\"title\":\"" + esc(t) + "\", \"description\":\"" + esc(d) + "\", \"status\":\"" + esc(s) + "\"}";
            String r = put_json("/jobs/edit/" + jid, b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Edit failed.";
        }
    }

    public static String drop_job(int jid) {
        try {
            String r = delete("/jobs/drop/" + jid);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Drop failed.";
        }
    }

    public static String push_cv(int uid, File f) {
        try {
            String bound = "Bound" + System.currentTimeMillis();
            URL url = new URL(BASE + "/resume/push_cv");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes("--" + bound + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"userId\"\r\n\r\n");
                out.writeBytes(uid + "\r\n");
                out.writeBytes("--" + bound + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"\r\n\r\n");
                Files.copy(f.toPath(), out);
                out.writeBytes("\r\n--" + bound + "--\r\n");
                out.flush();
            }

            String r = read_resp(conn);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            if (obj.has("error")) return obj.get("error").getAsString();
            if (conn.getResponseCode() >= 400) return "Upload failed (HTTP " + conn.getResponseCode() + ")";
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String make_letter(int uid, int jid) {
        try {
            String b = "{\"uid\":" + uid + "}";
            String r = post_json("/jobs/gen_letter/" + jid, b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("msg")) return obj.get("msg").getAsString();
            return "Failed.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String apply_to_job(int uid, int jid, int sc) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("uid", uid);
            data.addProperty("tech_sc", sc);
            String r = post_json("/jobs/apply_to/" + jid, data.toString());
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "App failed.";
        }
    }

    public static JsonObject get_adm_stats() {
        try {
            String r = get("/admin/stats_dash");
            return JsonParser.parseString(r).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray usr_history(int uid) {
        try {
            return JsonParser.parseString(get("/usr/history/" + uid)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static JsonArray list_apps(int jid) {
        try {
            return JsonParser.parseString(get("/jobs/apps_for/" + jid)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static String pick_app(int aid) {
        try {
            String r = post_json("/applicants/ok/" + aid, "{}");
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Failed.";
        }
    }

    public static JsonObject get_cv_stats(int uid) {
        try {
            String r = get("/usr/cv_stats/" + uid);
            return JsonParser.parseString(r).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String set_profile(int uid, String name, String mail, String bio) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("full_name", name);
            data.addProperty("email",     mail);
            data.addProperty("bio",       bio);
            String r = put_json("/users/edit/" + uid, data.toString());
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Up failed.";
        }
    }

    public static JsonObject see_profile(int uid) {
        try {
            String r = get("/users/see/" + uid);
            return JsonParser.parseString(r).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray list_notifs(int uid) {
        try {
            return JsonParser.parseString(get("/notifications/new_for/" + uid)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    public static void done_notif(int nid) {
        try { post_json("/notifications/dismiss/" + nid, "{}"); } catch (Exception ignored) {}
    }

    public static String add_comment(int aid, String msg) {
        try {
            String b = "{\"feedback\":\"" + esc(msg) + "\"}";
            String r = post_json("/applications/comment/" + aid, b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Failed.";
        }
    }
 
    public static String set_app_status(int aid, String s) {
        try {
            String b = "{\"status\":\"" + esc(s) + "\"}";
            String r = put_json("/applications/set_state/" + aid, b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Stat failed.";
        }
    }

    public static String set_app_vibes(int aid, String v) {
        try {
            String b = "{\"aid\":" + aid + ", \"v\":\"" + esc(v) + "\"}";
            String r = post_json("/applications/set_vibes", b);
            return r;
        } catch (Exception e) {
            return "Fail.";
        }
    }
 
    public static String put_note(int uid, String n) {
        try {
            String b = "{\"note\":\"" + esc(n) + "\"}";
            String r = post_json("/usr/notes/" + uid, b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Note failed.";
        }
    }
 
    public static JsonArray list_notes(int uid) {
        try {
            return JsonParser.parseString(get("/usr/notes/" + uid)).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }
 
    public static byte[] get_pdf(int uid) {
        try {
            URL url = new URL(BASE + "/resume/get_pdf/" + uid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buff = new byte[8192];
                    int n;
                    while ((n = is.read(buff)) != -1) baos.write(buff, 0, n);
                    return baos.toByteArray();
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    public static JsonArray get_trajectory(int uid) {
        try {
            return JsonParser.parseString(get("/usr/trajectory/" + uid)).getAsJsonArray();
        } catch (Exception e) { return new JsonArray(); }
    }

    public static JsonArray get_battle_users() {
        try {
            return JsonParser.parseString(get("/battle/users")).getAsJsonArray();
        } catch (Exception e) { return new JsonArray(); }
    }

    public static JsonObject run_battle(int uid1, int uid2, int jid) {
        try {
            return JsonParser.parseString(get("/battle/" + uid1 + "/vs/" + uid2 + "/for/" + jid)).getAsJsonObject();
        } catch (Exception e) { return new JsonObject(); }
    }

    public static String pass_forgot(String u) {
        try {
            String b = "{\"username\":\"" + esc(u) + "\"}";
            String r = post_json("/auth/lost_password", b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Fail.";
        }
    }

    public static String pass_reset(String tk, String pass) {
        try {
            String b = "{\"token\":\"" + esc(tk) + "\",\"password\":\"" + esc(pass) + "\"}";
            String r = post_json("/auth/reset_now", b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return obj.get("err").getAsString();
            return null;
        } catch (Exception e) {
            return "Fail.";
        }
    }

    public static int social_in(String prov, String mail) {
        try {
            String b = "{\"provider\":\"" + prov + "\",\"email\":\"" + mail + "\"}";
            String r = post_json("/auth/social_sign_in", b);
            JsonObject obj = JsonParser.parseString(r).getAsJsonObject();
            if (obj.has("err")) return -1;
            ResudexApp.usr = obj.get("usr").getAsString();
            return obj.get("uid").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }

    private static String post_json(String end, String b) throws Exception {
        return push_json(end, "POST", b);
    }

    private static String put_json(String end, String b) throws Exception {
        return push_json(end, "PUT", b);
    }

    private static String push_json(String end, String m, String b) throws Exception {
        URL url = new URL(BASE + end);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(m);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(b.getBytes("UTF-8")); }
        return read_resp(conn);
    }

    private static String get(String end) throws Exception {
        URL url = new URL(BASE + end);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return read_resp(conn);
    }

    private static String delete(String end) throws Exception {
        URL url = new URL(BASE + end);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        return read_resp(conn);
    }

    private static String read_resp(HttpURLConnection conn) throws Exception {
        InputStream s = null;
        try { s = conn.getInputStream(); } catch (IOException e) { s = conn.getErrorStream(); }
        if (s == null) return "{}";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = br.readLine()) != null) sb.append(l);
            return sb.toString();
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
