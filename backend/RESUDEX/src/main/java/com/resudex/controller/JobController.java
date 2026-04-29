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

// jobs and applications
@RestController
@CrossOrigin
public class JobController {

    @Autowired
    private DatabaseService db;

    private final ResumeScorer scorer = new ResumeScorer();

    // list all jobs
    @GetMapping("/api/jobs/list")
    public List<Map<String, Object>> getJobs(@RequestParam(required = false) boolean is_adm) {
        return db.getJobs(is_adm);
    }

    // get matched jobs for user
    @GetMapping("/api/jobs/matched_for/{uid}")
    public ResponseEntity<?> getMatches(@PathVariable int uid) {
        try {
            List<Map<String, Object>> raw = db.getJobs(false);

            Map<String, Map<String, Object>> m = new LinkedHashMap<>();
            for (Map<String, Object> j : raw) {
                String t = (String) j.get("title");
                if (t == null) t = (String) j.get("TITLE");
                if (t != null && !m.containsKey(t)) {
                    m.put(t, new LinkedHashMap<>(j));
                }
            }
            List<Map<String, Object>> jobs = new ArrayList<>(m.values());

            Map<String, Object> u = db.getUser(uid);

            String txt = null;
            String fn = "cv.pdf";
            if (u != null) {
                for (Map.Entry<String, Object> entry : u.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("resume_text") && entry.getValue() != null) {
                        txt = getRawText(entry.getValue());
                    }
                    if (entry.getKey().equalsIgnoreCase("resume_filename") && entry.getValue() != null) {
                        fn = entry.getValue().toString();
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

                    ResumeScore res = scorer.scan(fn, txt, jd);
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

    // create job
    @PostMapping("/api/jobs/create")
    public ResponseEntity<Map<String, Object>> createJob(@RequestBody Map<String, String> payload) {
        String t = payload.get("title");
        String d = payload.get("description");
        String s = payload.getOrDefault("status", "OPEN");

        if (t == null || t.isBlank() || d == null || d.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Fields missing"));
        }

        db.postJob(t.trim(), d.trim(), s.trim().toUpperCase());
        return ResponseEntity.ok(Map.of("msg", "Job saved"));
    }

    // edit job
    @PutMapping("/api/jobs/edit/{jid}")
    public ResponseEntity<Map<String, Object>> editJob(@PathVariable int jid, @RequestBody Map<String, String> payload) {
        String t = payload.get("title");
        String d = payload.get("description");
        String s = payload.getOrDefault("status", "OPEN");

        if (t == null || t.isBlank() || d == null || d.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Fields missing"));
        }

        db.editJob(jid, t.trim(), d.trim(), s.trim().toUpperCase());
        return ResponseEntity.ok(Map.of("msg", "Updated"));
    }

    // delete job
    @DeleteMapping("/api/jobs/drop/{jid}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable int jid) {
        db.deleteJob(jid);
        return ResponseEntity.ok(Map.of("msg", "Removed"));
    }

    // apply to job
    @PostMapping("/api/jobs/apply_to/{jid}")
    public ResponseEntity<Map<String, Object>> applyJob(
            @PathVariable int jid,
            @RequestBody Map<String, Object> body
    ) {
        int uid = ((Number) body.get("uid")).intValue();
        int techSc = body.containsKey("tech_sc") ? ((Number) body.get("tech_sc")).intValue() : -1;

        Map<String, Object> usr = db.getUser(uid);
        if (usr == null) return ResponseEntity.badRequest().body(Map.of("err", "No user"));

        String txt = getRawText(usr.get("resume_text"));
        if (txt == null || txt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Upload CV first!"));
        }

        boolean ok = db.apply(uid, jid, techSc);
        if (ok) return ResponseEntity.ok(Map.of("msg", "Success"));
        else return ResponseEntity.badRequest().body(Map.of("err", "Already applied"));
    }

    // get apps for job
    @GetMapping("/api/jobs/apps_for/{jid}")
    public List<Map<String, Object>> getApps(@PathVariable int jid) {
        Map<String, Object> job = db.getJob(jid);
        if (job == null) return Collections.emptyList();

        String jd = (String) job.get("description");
        List<Map<String, Object>> apps = db.getApps(jid);
        List<Map<String, Object>> out = new ArrayList<>();

        for (Map<String, Object> a : apps) {
            String txt = getRawText(a.get("resume_text"));
            Map<String, Object> row = new LinkedHashMap<>(a);
            String f = (String) a.getOrDefault("resume_filename", "cv.pdf");

            if (txt != null && !txt.isBlank()) {
                ResumeScore res = scorer.scan(f, txt, jd);
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

    // shortlist applicant
    @PostMapping("/api/applicants/ok/{aid}")
    public ResponseEntity<Map<String, Object>> shortlist(@PathVariable int aid) {
        db.shortlist(aid);
        int uid = db.getUid(aid);
        if (uid != -1) db.notify(uid, "🎉 Shortlisted!");
        return ResponseEntity.ok(Map.of("msg", "Done"));
    }

    // send feedback
    @PostMapping("/api/applications/comment/{aid}")
    public ResponseEntity<Map<String, Object>> sendFeedback(@PathVariable int aid, @RequestBody Map<String, String> body) {
        String fbk = body.get("feedback");
        if (fbk == null) return ResponseEntity.badRequest().body(Map.of("err", "No text"));
        db.setFeedback(aid, fbk.trim());
        return ResponseEntity.ok(Map.of("msg", "Sent"));
    }

    // set app status
    @PutMapping("/api/applications/set_state/{aid}")
    public ResponseEntity<Map<String, Object>> setAppStatus(@PathVariable int aid, @RequestBody Map<String, String> payload) {
        String s = payload.get("status");
        if (s == null || s.isBlank()) return ResponseEntity.badRequest().body(Map.of("err", "No stat"));
        db.setStatus(aid, s.trim().toUpperCase());

        int uid = db.getUid(aid);
        if (uid != -1) {
            String msg = "📋 Update: " + s.toUpperCase();
            if (s.equalsIgnoreCase("HIRED")) msg = "🎊 HIRED! Awesome!";
            db.notify(uid, msg);
        }
        return ResponseEntity.ok(Map.of("msg", "Updated"));
    }

    // add admin note
    @PostMapping("/api/usr/notes/{uid}")
    public ResponseEntity<Map<String, Object>> addNote(@PathVariable int uid, @RequestBody Map<String, String> body) {
        String n = body.get("note");
        if (n == null) return ResponseEntity.badRequest().body(Map.of("err", "No note"));
        db.addNote(uid, n);
        return ResponseEntity.ok(Map.of("msg", "Saved"));
    }

    // set vibes tag
    @PostMapping("/api/applications/set_vibes")
    public ResponseEntity<String> setVibes(@RequestBody Map<String, Object> body) {
        int aid = (int) body.get("aid");
        String v = (String) body.get("v");
        db.setVibes(aid, v);
        return ResponseEntity.ok("Vibes saved.");
    }

    // get notes
    @GetMapping("/api/usr/notes/{uid}")
    public List<Map<String, Object>> getNotes(@PathVariable int uid) {
        return db.getNotes(uid);
    }

    // download pdf report
    @GetMapping("/api/jobs/report/{jid}/{aid}")
    public ResponseEntity<byte[]> getReport(@PathVariable int jid, @PathVariable int aid) {
        Map<String, Object> job = db.getJob(jid);
        List<Map<String, Object>> apps = db.getApps(jid);
        Map<String, Object> a = apps.stream()
            .filter(item -> ((Number)item.get("app_id")).intValue() == aid)
            .findFirst().orElse(null);

        if (job == null || a == null) return ResponseEntity.notFound().build();

        String txt = getRawText(a.get("resume_text"));
        String title = (String) job.get("title");
        String name = (String) a.get("username");

        ResumeScore res = scorer.scan("cv.pdf", txt, (String) job.get("description"));
        byte[] pdf = PdfUtil.generateShortlistReport(res, name, title);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rep_" + name + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // user application history
    @GetMapping("/api/usr/history/{uid}")
    public List<Map<String, Object>> getHistory(@PathVariable int uid) {
        return db.getUserApps(uid);
    }

    // cv stats
    @GetMapping("/api/usr/cv_stats/{uid}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable int uid) {
        Map<String, Object> u = db.getUser(uid);
        if (u == null || u.get("resume_text") == null) {
            return ResponseEntity.badRequest().body(Map.of("err", "No CV found"));
        }

        String txt = getRawText(u.get("resume_text"));
        String f = (String) u.getOrDefault("resume_filename", "cv.pdf");
        ResumeScore res = scorer.scan(f, txt, "");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("domain_fit", res.get_domains());
        out.put("role_fit", res.get_roles());
        out.put("exp_yrs", res.get_exp());

        List<String> top = res.get_domains().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted((a, b) -> b.getValue() - a.getValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        out.put("top_doms", top);
        return ResponseEntity.ok(out);
    }

    // generate cover letter
    @PostMapping("/api/jobs/gen_letter/{jid}")
    public ResponseEntity<Map<String, Object>> genLetter(@PathVariable int jid, @RequestBody Map<String, Integer> payload) {
        int uid = payload.get("uid");
        Map<String, Object> job = db.getJob(jid);
        Map<String, Object> user = db.getUser(uid);

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

    // skill trajectory
    @GetMapping("/api/usr/trajectory/{uid}")
    public ResponseEntity<List<Map<String, Object>>> getTrajectory(@PathVariable int uid) {
        return ResponseEntity.ok(db.getSnapshots(uid));
    }

    // battle users list
    @GetMapping("/api/battle/users")
    public ResponseEntity<List<Map<String, Object>>> getBattleUsers() {
        List<Map<String, Object>> users = db.getUsersWithResume();
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

    // run resume battle
    @GetMapping("/api/battle/{uid1}/vs/{uid2}/for/{jid}")
    public ResponseEntity<Map<String, Object>> runBattle(
            @PathVariable int uid1, @PathVariable int uid2, @PathVariable int jid) {
        Map<String, Object> job = db.getJob(jid);
        if (job == null) return ResponseEntity.badRequest().body(Map.of("error", "Job not found"));

        String jd = job.get("title") + "\n" + job.get("description");

        Map<String, Object> u1 = db.getUser(uid1);
        Map<String, Object> u2 = db.getUser(uid2);
        if (u1 == null || u2 == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        String txt1 = getRawText(u1.get("resume_text"));
        String txt2 = getRawText(u2.get("resume_text"));
        String f1   = u1.getOrDefault("resume_filename", "cv.pdf").toString();
        String f2   = u2.getOrDefault("resume_filename", "cv.pdf").toString();

        ResumeScore r1 = scorer.scan(f1, txt1, jd);
        ResumeScore r2 = scorer.scan(f2, txt2, jd);

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

    // debug user
    @GetMapping("/api/debug/user/{uid}")
    public ResponseEntity<Map<String, Object>> debugUser(@PathVariable int uid) {
        Map<String, Object> u = db.getUser(uid);
        if (u == null) return ResponseEntity.ok(Map.of("error", "user not found"));
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : u.entrySet()) {
            if (e.getKey().equalsIgnoreCase("resume_text")) {
                String txt = getRawText(e.getValue());
                out.put(e.getKey(), txt == null ? "NULL" : "LENGTH=" + txt.length() + " PREVIEW=" + txt.substring(0, Math.min(100, txt.length())));
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return ResponseEntity.ok(out);
    }

    // admin stats dashboard
    @GetMapping("/api/admin/stats_dash")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        List<Map<String, Object>> jobs = db.getJobs(true);
        int total = 0, picked = 0;
        Map<String, Integer> trends = new HashMap<>();

        for (Map<String, Object> j : jobs) {
            int jid = (int) j.get("id");
            List<Map<String, Object>> apps = db.getApps(jid);
            total += apps.size();
            for (Map<String, Object> a : apps) {
                if ("SELECTED".equalsIgnoreCase((String)a.get("status"))) picked++;
            }
            String jd = (String) j.get("description");
            Set<String> s = scorer.scan("", "", jd).get_hits();
            for (String item : s) trends.put(item, trends.getOrDefault(item, 0) + 1);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("jobs_cnt", jobs.size());
        res.put("apps_cnt", total);
        res.put("picked_cnt", picked);
        res.put("trends", trends);

        return ResponseEntity.ok(res);
    }

    // extract text from clob or string
    private String getRawText(Object obj) {
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
