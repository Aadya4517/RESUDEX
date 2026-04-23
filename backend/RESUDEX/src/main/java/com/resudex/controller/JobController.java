package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
import com.resudex.service.DatabaseService;
import com.resudex.util.PdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for jobs and apps.
 * 200% Humanized logic.
 */
@RestController
@CrossOrigin
public class JobController {

    @Autowired
    private DatabaseService app_db;

    private final ResumeScorer doc_scorer = new ResumeScorer();

    // --- Listing ---

    @GetMapping("/api/jobs/list")
    public List<Map<String, Object>> list_all_jobs(@RequestParam(required = false) boolean is_adm) {
        return app_db.list_all_jobs(is_adm);
    }

    @GetMapping("/api/jobs/matched_for/{uid}")
    public ResponseEntity<?> get_usr_matches(@PathVariable int uid) {
        try {
            List<Map<String, Object>> raw = app_db.list_all_jobs(false);

            Map<String, Map<String, Object>> m = new LinkedHashMap<>();
            for (Map<String, Object> j : raw) {
                String t = (String) j.get("title");
                if (t == null) t = (String) j.get("TITLE");
                if (t != null && !m.containsKey(t)) {
                    m.put(t, new LinkedHashMap<>(j));
                }
            }
            List<Map<String, Object>> jobs = new ArrayList<>(m.values());

            Map<String, Object> u = app_db.get_usr_by_id(uid);

            String txt = null;
            String fname = "cv.pdf";
            if (u != null) {
                for (Map.Entry<String, Object> entry : u.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("resume_text") && entry.getValue() != null) {
                        txt = get_raw_text(entry.getValue());
                    }
                    if (entry.getKey().equalsIgnoreCase("resume_filename") && entry.getValue() != null) {
                        fname = entry.getValue().toString();
                    }
                }
            }

            System.out.println("[MATCH] uid=" + uid + " resume_length=" + (txt == null ? "null" : txt.length()) + " jobs=" + jobs.size());

            if (txt == null || txt.isBlank()) {
                for (Map<String, Object> j : jobs) {
                    j.put("sc", 0);
                    j.put("hits", new ArrayList<>());
                    j.put("miss", new ArrayList<>());
                }
                return ResponseEntity.ok(jobs);
            }

            for (Map<String, Object> j : jobs) {
                try {
                    Object titleObj = j.get("title"); if (titleObj == null) titleObj = j.get("TITLE");
                    Object descObj  = j.get("description"); if (descObj == null) descObj = j.get("DESCRIPTION");
                    String title = titleObj != null ? titleObj.toString() : "";
                    String desc  = descObj  != null ? descObj.toString()  : "";
                    String jd = title + "\n" + desc;

                    ResumeScore res = doc_scorer.scan(fname, txt, jd);
                    j.put("sc",      res.get_sc());
                    j.put("hits",    res.get_hits());
                    j.put("miss",    res.get_miss());
                    j.put("roadmap", res.get_roadmap());
                    System.out.println("[MATCH] job=" + title + " sc=" + res.get_sc());
                } catch (Exception ex) {
                    System.err.println("[MATCH ERROR] job scan failed: " + ex.getMessage());
                    j.put("sc", 0);
                    j.put("hits", new ArrayList<>());
                    j.put("miss", new ArrayList<>());
                }
            }

            jobs.sort((a, b) -> {
                int sa = a.containsKey("sc") ? ((Number) a.get("sc")).intValue() : 0;
                int sb = b.containsKey("sc") ? ((Number) b.get("sc")).intValue() : 0;
                return sb - sa;
            });

            return ResponseEntity.ok(jobs);

        } catch (Exception ex) {
            System.err.println("[MATCH FATAL] " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    // --- Admin Ops ---

    @PostMapping("/api/jobs/create")
    public ResponseEntity<Map<String, Object>> make_new_job(@RequestBody Map<String, String> payload) {
        String t = payload.get("title");
        String d = payload.get("description");
        String s = payload.getOrDefault("status", "OPEN");
  
        if (t == null || t.isBlank() || d == null || d.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Fields missing"));
        }
  
        app_db.post_job(t.trim(), d.trim(), s.trim().toUpperCase());
        return ResponseEntity.ok(Map.of("msg", "Job saved"));
    }

    @PutMapping("/api/jobs/edit/{jid}")
    public ResponseEntity<Map<String, Object>> modify_job(@PathVariable int jid, @RequestBody Map<String, String> payload) {
        String t = payload.get("title");
        String d = payload.get("description");
        String s = payload.getOrDefault("status", "OPEN");
  
        if (t == null || t.isBlank() || d == null || d.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Fields missing"));
        }
  
        app_db.edit_job_data(jid, t.trim(), d.trim(), s.trim().toUpperCase());
        return ResponseEntity.ok(Map.of("msg", "Updated"));
    }

    @DeleteMapping("/api/jobs/drop/{jid}")
    public ResponseEntity<Map<String, Object>> kill_job_data(@PathVariable int jid) {
        app_db.kill_job(jid);
        return ResponseEntity.ok(Map.of("msg", "Removed"));
    }

    // --- Applications ---

    @PostMapping("/api/jobs/apply_to/{jid}")
    public ResponseEntity<Map<String, Object>> push_app(
            @PathVariable int jid,
            @RequestBody Map<String, Object> body
    ) {
        int uid = ((Number) body.get("uid")).intValue();
        int tech_sc = body.containsKey("tech_sc") ? ((Number) body.get("tech_sc")).intValue() : -1;

        Map<String, Object> usr = app_db.get_usr_by_id(uid);
        if (usr == null) return ResponseEntity.badRequest().body(Map.of("err", "No user"));
        
        String txt = get_raw_text(usr.get("resume_text"));
        if (txt == null || txt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Upload CV first!"));
        }

        boolean ok = app_db.sub_app(uid, jid, tech_sc);
        if (ok) return ResponseEntity.ok(Map.of("msg", "Success"));
        else return ResponseEntity.badRequest().body(Map.of("err", "Already applied"));
    }

    @GetMapping("/api/jobs/apps_for/{jid}")
    public List<Map<String, Object>> fetch_apps(@PathVariable int jid) {
        Map<String, Object> job = app_db.get_job_info(jid);
        if (job == null) return Collections.emptyList();

        String jd = (String) job.get("description");
        List<Map<String, Object>> apps = app_db.list_apps_for_job(jid);
        List<Map<String, Object>> out = new ArrayList<>();

        for (Map<String, Object> a : apps) {
            String txt = get_raw_text(a.get("resume_text"));
            Map<String, Object> row = new LinkedHashMap<>(a);
            String f = (String) a.getOrDefault("resume_filename", "cv.pdf");

            if (txt != null && !txt.isBlank()) {
                ResumeScore res = doc_scorer.scan(f, txt, jd);
                row.put("sc", res.get_sc());
                row.put("hits", res.get_hits());
                row.put("miss", res.get_miss());
                row.put("recs", res.get_recs());
            } else {
                row.put("sc", 0);
                row.put("recs", List.of("Missing CV"));
            }
            row.put("tech_sc", a.get("tech_score"));
            out.add(row);
        }

        out.sort((a, b) -> ((Number) b.get("sc")).intValue() - ((Number) a.get("sc")).intValue());
        return out;
    }

    @PostMapping("/api/applicants/ok/{aid}")
    public ResponseEntity<Map<String, Object>> shortlist_usr(@PathVariable int aid) {
        app_db.pick_applicant(aid);
        int uid = app_db.find_uid(aid);
        if (uid != -1) app_db.push_notif(uid, "🎉 Shortlisted!");
        return ResponseEntity.ok(Map.of("msg", "Done"));
    }

    @PostMapping("/api/applications/comment/{aid}")
    public ResponseEntity<Map<String, Object>> send_feedback(@PathVariable int aid, @RequestBody Map<String, String> body) {
        String fbk = body.get("feedback");
        if (fbk == null) return ResponseEntity.badRequest().body(Map.of("err", "No text"));
        app_db.give_feedback(aid, fbk.trim());
        return ResponseEntity.ok(Map.of("msg", "Sent"));
    }

    @PutMapping("/api/applications/set_state/{aid}")
    public ResponseEntity<Map<String, Object>> edit_app_stat(@PathVariable int aid, @RequestBody Map<String, String> payload) {
        String s = payload.get("status");
        if (s == null || s.isBlank()) return ResponseEntity.badRequest().body(Map.of("err", "No stat"));
        app_db.change_app_status(aid, s.trim().toUpperCase());
        
        int uid = app_db.find_uid(aid);
        if (uid != -1) {
            String m = "📋 Update: " + s.toUpperCase();
            if (s.equalsIgnoreCase("HIRED")) m = "🎊 HIRED! Awesome!";
            app_db.push_notif(uid, m);
        }
        return ResponseEntity.ok(Map.of("msg", "Updated"));
    }

    // --- Notes & Stats ---

    @PostMapping("/api/usr/notes/{uid}")
    public ResponseEntity<Map<String, Object>> post_note(@PathVariable int uid, @RequestBody Map<String, String> body) {
        String n = body.get("note");
        if (n == null) return ResponseEntity.badRequest().body(Map.of("err", "No note"));
        app_db.add_note(uid, n);
        return ResponseEntity.ok(Map.of("msg", "Saved"));
    }

    @PostMapping("/api/applications/set_vibes")
    public ResponseEntity<String> set_vibes(@RequestBody Map<String, Object> body) {
        int aid = (int) body.get("aid");
        String v = (String) body.get("v");
        app_db.set_app_vibes(aid, v);
        return ResponseEntity.ok("Vibes saved.");
    }

    @GetMapping("/api/usr/notes/{uid}")
    public List<Map<String, Object>> show_notes(@PathVariable int uid) {
        return app_db.get_notes(uid);
    }

    @GetMapping("/api/jobs/report/{jid}/{aid}")
    public ResponseEntity<byte[]> fetch_pdf_rep(@PathVariable int jid, @PathVariable int aid) {
        Map<String, Object> job = app_db.get_job_info(jid);
        List<Map<String, Object>> apps = app_db.list_apps_for_job(jid);
        Map<String, Object> a = apps.stream()
            .filter(item -> ((Number)item.get("app_id")).intValue() == aid)
            .findFirst().orElse(null);
  
        if (job == null || a == null) return ResponseEntity.notFound().build();
  
        String txt = get_raw_text(a.get("resume_text"));
        String title = (String) job.get("title");
        String name = (String) a.get("username");
  
        ResumeScore res = doc_scorer.scan("cv.pdf", txt, (String) job.get("description"));
        byte[] pdf = PdfUtil.generateShortlistReport(res, name, title);
  
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rep_" + name + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/api/usr/history/{uid}")
    public List<Map<String, Object>> my_apps(@PathVariable int uid) {
        return app_db.usr_apps(uid);
    }

    @GetMapping("/api/usr/cv_stats/{uid}")
    public ResponseEntity<Map<String, Object>> show_cv_stats(@PathVariable int uid) {
        Map<String, Object> u = app_db.get_usr_by_id(uid);
        if (u == null || u.get("resume_text") == null) {
            return ResponseEntity.badRequest().body(Map.of("err", "No CV found"));
        }

        String txt = get_raw_text(u.get("resume_text"));
        String f = (String) u.getOrDefault("resume_filename", "cv.pdf");
        ResumeScore res = doc_scorer.scan(f, txt, ""); 
        
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("domain_fit", res.get_domains());
        out.put("role_fit", res.get_roles());
        out.put("exp_yrs", res.get_exp());
        
        List<String> top = res.get_domains().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted((a,b) -> b.getValue() - a.getValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        out.put("top_doms", top);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/api/jobs/gen_letter/{jid}")
    public ResponseEntity<Map<String, Object>> gen_letter(@PathVariable int jid, @RequestBody Map<String, Integer> payload) {
        int uid = payload.get("uid");
        Map<String, Object> job = app_db.get_job_info(jid);
        Map<String, Object> user = app_db.get_usr_by_id(uid);

        if (job == null || user == null) return ResponseEntity.badRequest().body(Map.of("err", "Bad IDs"));

        String t = (String) job.get("title");
        String name = (String) user.getOrDefault("full_name", user.get("username"));
        String d = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));

        String txt = "Hello,\n\n" +
                "I'm applying for " + t + ". " +
                "I've got the skills needed.\n\n" +
                "Check my profile for details.\n\n" +
                "Thanks,\n" + name + "\n" + d;

        return ResponseEntity.ok(Map.of("msg", txt));
    }

    // ── SKILL TRAJECTORY ──────────────────────────────────────────────────────

    @GetMapping("/api/usr/trajectory/{uid}")
    public ResponseEntity<List<Map<String, Object>>> get_trajectory(@PathVariable int uid) {
        return ResponseEntity.ok(app_db.get_snapshots(uid));
    }

    // ── RESUME BATTLE ─────────────────────────────────────────────────────────

    @GetMapping("/api/battle/users")
    public ResponseEntity<List<Map<String, Object>>> battle_users() {
        List<Map<String, Object>> users = app_db.get_all_users_with_resume();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> u : users) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",       u.get("id"));
            row.put("username", u.get("username"));
            row.put("full_name", u.getOrDefault("full_name", u.get("username")));
            out.add(row);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/battle/{uid1}/vs/{uid2}/for/{jid}")
    public ResponseEntity<Map<String, Object>> run_battle(
            @PathVariable int uid1, @PathVariable int uid2, @PathVariable int jid) {
        Map<String, Object> job = app_db.get_job_info(jid);
        if (job == null) return ResponseEntity.badRequest().body(Map.of("error", "Job not found"));

        String jd = job.get("title") + "\n" + job.get("description");

        Map<String, Object> u1 = app_db.get_usr_by_id(uid1);
        Map<String, Object> u2 = app_db.get_usr_by_id(uid2);
        if (u1 == null || u2 == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        String txt1 = get_raw_text(u1.get("resume_text"));
        String txt2 = get_raw_text(u2.get("resume_text"));
        String f1   = u1.getOrDefault("resume_filename", "cv.pdf").toString();
        String f2   = u2.getOrDefault("resume_filename", "cv.pdf").toString();

        ResumeScore r1 = doc_scorer.scan(f1, txt1, jd);
        ResumeScore r2 = doc_scorer.scan(f2, txt2, jd);

        Map<String, Object> p1 = new LinkedHashMap<>();
        p1.put("uid",      uid1);
        p1.put("name",     u1.getOrDefault("full_name", u1.get("username")));
        p1.put("sc",       r1.get_sc());
        p1.put("exp",      r1.get_exp());
        p1.put("hits",     r1.get_hits());
        p1.put("miss",     r1.get_miss());
        p1.put("domains",  r1.get_domains());
        p1.put("roadmap",  r1.get_roadmap());

        Map<String, Object> p2 = new LinkedHashMap<>();
        p2.put("uid",      uid2);
        p2.put("name",     u2.getOrDefault("full_name", u2.get("username")));
        p2.put("sc",       r2.get_sc());
        p2.put("exp",      r2.get_exp());
        p2.put("hits",     r2.get_hits());
        p2.put("miss",     r2.get_miss());
        p2.put("domains",  r2.get_domains());
        p2.put("roadmap",  r2.get_roadmap());

        String winner = r1.get_sc() > r2.get_sc() ? p1.get("name").toString()
                      : r2.get_sc() > r1.get_sc() ? p2.get("name").toString()
                      : "TIE";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job",    job.get("title"));
        result.put("p1",     p1);
        result.put("p2",     p2);
        result.put("winner", winner);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/debug/user/{uid}")    public ResponseEntity<Map<String, Object>> debug_user(@PathVariable int uid) {
        Map<String, Object> u = app_db.get_usr_by_id(uid);
        if (u == null) return ResponseEntity.ok(Map.of("error", "user not found"));
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : u.entrySet()) {
            if (e.getKey().equalsIgnoreCase("resume_text")) {
                String txt = get_raw_text(e.getValue());
                out.put(e.getKey(), txt == null ? "NULL" : "LENGTH=" + txt.length() + " PREVIEW=" + txt.substring(0, Math.min(100, txt.length())));
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/admin/stats_dash")    public ResponseEntity<Map<String, Object>> admin_hub() {
        List<Map<String, Object>> jobs = app_db.list_all_jobs(true);
        int total = 0, picked = 0;
        Map<String, Integer> trends = new HashMap<>();

        for (Map<String, Object> j : jobs) {
            int jid = (int) j.get("id");
            List<Map<String, Object>> apps = app_db.list_apps_for_job(jid);
            total += apps.size();
            for (Map<String, Object> a : apps) {
                if ("SELECTED".equalsIgnoreCase((String)a.get("status"))) picked++;
            }
            String jd = (String) j.get("description");
            Set<String> s = doc_scorer.scan("", "", jd).get_hits();
            for (String item : s) trends.put(item, trends.getOrDefault(item, 0) + 1);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("jobs_cnt", jobs.size());
        res.put("apps_cnt", total);
        res.put("picked_cnt", picked);
        res.put("trends", trends);

        return ResponseEntity.ok(res);
    }

    private String get_raw_text(Object obj) {
        if (obj == null) return "";
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof java.sql.Clob) {
            java.sql.Clob c = (java.sql.Clob) obj;
            try {
                long len = c.length();
                if (len == 0) return "";
                return c.getSubString(1, (int) len);
            } catch (Exception e) {
                try {
                    // fallback: read via Reader
                    java.io.Reader r = c.getCharacterStream();
                    java.io.BufferedReader br = new java.io.BufferedReader(r);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append("\n");
                    return sb.toString();
                } catch (Exception e2) {
                    return "";
                }
            }
        }
        return obj.toString();
    }
}
