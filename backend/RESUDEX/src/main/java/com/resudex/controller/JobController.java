package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * JobController - manage jobs, applications, admin shortlisting.
 */
@RestController
@CrossOrigin
public class JobController {

    @Autowired
    private DatabaseService db;

    private final ResumeScorer scorer = new ResumeScorer();

    // -------- List all jobs --------
    @GetMapping("/api/jobs")
    public List<Map<String, Object>> getAllJobs() {
        return db.getAllJobs();
    }

    // -------- List all jobs with personal scores --------
    @GetMapping("/api/jobs/matched/{userId}")
    public List<Map<String, Object>> getMatchedJobs(@PathVariable int userId) {
        List<Map<String, Object>> jobs = db.getAllJobs();
        Map<String, Object> user = db.getUserById(userId);
        
        if (user == null || user.get("resume_text") == null) {
            return jobs;
        }

        String resumeText = (String) user.get("resume_text");
        String resumeFilename = (String) user.getOrDefault("resume_filename", "resume.pdf");

        for (Map<String, Object> job : jobs) {
            String jd = (String) job.get("description");
            ResumeScore score = scorer.scoreResume(resumeFilename, resumeText, jd);
            job.put("score", score.getFinalScore());
            job.put("matchedSkills", score.getMatchedSkills());
        }

        // Sort by score if possible
        jobs.sort((a, b) -> {
            int scoreA = a.containsKey("score") ? ((Number) a.get("score")).intValue() : 0;
            int scoreB = b.containsKey("score") ? ((Number) b.get("score")).intValue() : 0;
            return scoreB - scoreA;
        });

        return jobs;
    }

    // -------- Admin: Create a job --------
    @PostMapping("/api/jobs")
    public ResponseEntity<Map<String, Object>> createJob(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.get("description");

        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and description are required"));
        }

        db.createJob(title.trim(), description.trim());
        return ResponseEntity.ok(Map.of("message", "Job posted successfully"));
    }

    // -------- User: Apply for a job --------
    @PostMapping("/api/jobs/{jobId}/apply")
    public ResponseEntity<Map<String, Object>> applyForJob(
            @PathVariable int jobId,
            @RequestBody Map<String, Object> body
    ) {
        int userId = ((Number) body.get("userId")).intValue();

        // Check user has uploaded a resume
        Map<String, Object> user = db.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        String resumeText = (String) user.get("resume_text");
        if (resumeText == null || resumeText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload your resume before applying"));
        }

        boolean success = db.applyForJob(userId, jobId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Applied successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "You have already applied for this job"));
        }
    }

    // -------- Admin: Get ranked applicants for a job --------
    @GetMapping("/api/jobs/{jobId}/applicants")
    public List<Map<String, Object>> getApplicants(@PathVariable int jobId) {
        Map<String, Object> job = db.getJobById(jobId);
        if (job == null) return Collections.emptyList();

        String jobDescription = (String) job.get("description");
        List<Map<String, Object>> applicants = db.getApplicantsForJob(jobId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> applicant : applicants) {
            String resumeText    = (String) applicant.getOrDefault("resume_text", "");
            String resumeFilename = (String) applicant.getOrDefault("resume_filename", "resume.pdf");

            if (resumeText == null) resumeText = "";

            Map<String, Object> row = new LinkedHashMap<>(applicant);

            if (!resumeText.isBlank()) {
                ResumeScore score = scorer.scoreResume(resumeFilename, resumeText, jobDescription);
                row.put("score",          score.getFinalScore());
                row.put("matchedSkills",  score.getMatchedSkills());
                row.put("missingSkills",  score.getMissingSkills());
                row.put("rankSummary",    score.getRankSummary());
            } else {
                row.put("score",          0);
                row.put("matchedSkills",  Collections.emptyList());
                row.put("missingSkills",  Collections.emptyList());
                row.put("rankSummary",    List.of("No resume uploaded"));
            }

            result.add(row);
        }

        // Sort by score descending (best candidate first)
        result.sort((a, b) ->
                ((Number) b.get("score")).intValue() - ((Number) a.get("score")).intValue()
        );

        return result;
    }

    // -------- Admin: Select / shortlist a candidate --------
    @PostMapping("/api/applicants/{applicationId}/select")
    public ResponseEntity<Map<String, Object>> selectApplicant(@PathVariable int applicationId) {
        db.selectApplicant(applicationId);
        return ResponseEntity.ok(Map.of("message", "Candidate selected successfully!"));
    }

    // -------- User: Get applied jobs --------
    @GetMapping("/api/jobs/applications/{userId}")
    public List<Map<String, Object>> getUserApplications(@PathVariable int userId) {
        return db.getUserApplications(userId);
    }
}
