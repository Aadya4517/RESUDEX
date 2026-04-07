package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        List<Map<String, Object>> rawJobs = db.getAllJobs();
        
        // Deduplicate Jobs (Handles accidental double-clicks in Admin panel from past sessions)
        Map<String, Map<String, Object>> uniqueJobsMap = new LinkedHashMap<>();
        for (Map<String, Object> job : rawJobs) {
            String title = (String) job.get("title");
            if (title != null && !uniqueJobsMap.containsKey(title)) {
                uniqueJobsMap.put(title, new LinkedHashMap<>(job));
            }
        }
        List<Map<String, Object>> jobs = new ArrayList<>(uniqueJobsMap.values());
        
        Map<String, Object> user = db.getUserById(userId);
        Object rtObj = null;
        if (user != null) {
            for (String k : user.keySet()) {
                if (k.equalsIgnoreCase("resume_text")) { rtObj = user.get(k); break; }
            }
        }

        if (user == null || rtObj == null) {
            // Even if no resume, provide a default score of 0 so they show up in 'Opportunities'
            for (Map<String, Object> job : jobs) {
                job.put("score", 0);
                job.put("matchedSkills", new ArrayList<>());
            }
            return jobs;
        }

        String resumeText = extractResumeText(rtObj);

        String resumeFilename = "resume.pdf";
        for (String k : user.keySet()) {
            if (k.equalsIgnoreCase("resume_filename")) { resumeFilename = (String) user.get(k); break; }
        }

        for (Map<String, Object> job : jobs) {
            String title = "";
            String desc  = "";
            for (String k : job.keySet()) {
                if (k.equalsIgnoreCase("title")) { title = (String) job.get(k); }
                if (k.equalsIgnoreCase("description")) { desc = (String) job.get(k); }
            }
            
            // Pass BOTH title and description to help with 'Direct Role Boost'
            String fullJD = title + "\n" + desc;
            
            ResumeScore score = scorer.scoreResume(resumeFilename, resumeText, fullJD);
            
            int finalScore = score.getFinalScore();
            job.put("score", finalScore);
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

    // -------- Admin: Update a job --------
    @PutMapping("/api/jobs/{id}")
    public ResponseEntity<Map<String, Object>> updateJob(@PathVariable int id, @RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.get("description");

        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and description are required"));
        }

        db.updateJob(id, title.trim(), description.trim());
        return ResponseEntity.ok(Map.of("message", "Job updated successfully"));
    }

    // -------- Admin: Delete a job --------
    @DeleteMapping("/api/jobs/{id}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable int id) {
        db.deleteJob(id);
        return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
    }

    // -------- User: Apply for a job --------
    @PostMapping("/api/jobs/{jobId}/apply")
    public ResponseEntity<Map<String, Object>> applyForJob(
            @PathVariable int jobId,
            @RequestBody Map<String, Object> body
    ) {
        int userId = ((Number) body.get("userId")).intValue();
        int techScore = body.containsKey("techScore") ? ((Number) body.get("techScore")).intValue() : -1;

        // Check user has uploaded a resume
        Map<String, Object> user = db.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        String resumeText = extractResumeText(user.get("resume_text"));
        if (resumeText == null || resumeText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload your resume before applying"));
        }

        boolean success = db.applyForJob(userId, jobId, techScore);
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
            String resumeText    = extractResumeText(applicant.get("resume_text"));
            if (resumeText == null) resumeText = "";

            Map<String, Object> row = new LinkedHashMap<>(applicant);

            String resumeFilename = (String) applicant.getOrDefault("resume_filename", "resume.pdf");

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

            // Include tech score from application table
            row.put("techScore", applicant.get("tech_score"));

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
        
        // Notify the user
        int userId = db.getApplicationUserId(applicationId);
        if (userId != -1) {
            db.addNotification(userId, "🎉 Congratulations! You have been SELECTED for a position. Check your applications!");
        }
        
        return ResponseEntity.ok(Map.of("message", "Candidate selected successfully!"));
    }

    // -------- Admin: Provide Feedback --------
    @PostMapping("/api/applications/{applicationId}/feedback")
    public ResponseEntity<Map<String, Object>> provideFeedback(
            @PathVariable int applicationId,
            @RequestBody Map<String, String> body
    ) {
        String feedback = body.get("feedback");
        if (feedback == null) return ResponseEntity.badRequest().body(Map.of("error", "Feedback required"));
        
        db.updateApplicationFeedback(applicationId, feedback.trim());
        return ResponseEntity.ok(Map.of("message", "Feedback saved! User will see it on their dashboard."));
    }

    // -------- Admin: Update application status (Kanban) --------
    @PutMapping("/api/applications/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable int applicationId,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status required"));
        }
        db.updateApplicationStatus(applicationId, status.trim().toUpperCase());
        
        int userId = db.getApplicationUserId(applicationId);
        if (userId != -1) {
            db.addNotification(userId, "📋 Your application status has been updated to: " + status.trim().toUpperCase());
        }
        
        return ResponseEntity.ok(Map.of("message", "Application status updated to " + status));
    }

    // -------- User: Get applied jobs --------
    @GetMapping("/api/jobs/applications/{userId}")
    public List<Map<String, Object>> getUserApplications(@PathVariable int userId) {
        return db.getUserApplications(userId);
    }

    // -------- Idea 2: User Resume Analytics --------
    @GetMapping("/api/users/{userId}/resume/analytics")
    public ResponseEntity<Map<String, Object>> getResumeAnalytics(@PathVariable int userId) {
        Map<String, Object> user = db.getUserById(userId);
        if (user == null || user.get("resume_text") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No resume found. Please sync your profile first."));
        }

        String text = extractResumeText(user.get("resume_text"));
        String filename = (String) user.getOrDefault("resume_filename", "resume.pdf");

        // Score against a blank JD to get global domain fits
        ResumeScore res = scorer.scoreResume(filename, text, ""); 
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("domainFit", res.getDomainFit());
        response.put("roleFit", res.getRoleFit());
        response.put("yearsOfExperience", res.getExperienceYears());
        
        // Find top skills from the domain maps
        List<String> topSkills = res.getDomainFit().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted((a,b) -> b.getValue() - a.getValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        response.put("topDomains", topSkills);
        return ResponseEntity.ok(response);
    }

    // -------- Idea 3: AI Cover Letter Generator --------
    @PostMapping("/api/jobs/{jobId}/cover-letter")
    public ResponseEntity<Map<String, Object>> generateCoverLetter(
            @PathVariable int jobId, 
            @RequestBody Map<String, Integer> body
    ) {
        int userId = body.get("userId");
        Map<String, Object> job = db.getJobById(jobId);
        Map<String, Object> user = db.getUserById(userId);

        if (job == null || user == null) return ResponseEntity.badRequest().body(Map.of("error", "Not found"));

        String title = (String) job.get("title");
        String uname = (String) user.get("username");
        String fullName = (String) user.getOrDefault("full_name", uname);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        String letter = "Dear Hiring Manager,\n\n" +
                "I am writing to express my strong interest in the " + title + " position at your company. " +
                "With my background in software development and my passion for building impactful solutions, " +
                "I believe my profile is a great fit for your team's current needs.\n\n" +
                "Having worked on various projects, I have developed a solid foundation in the core technologies " +
                "required for this role. I am particularly impressed by your company's commitment to innovation " +
                "and would welcome the opportunity to contribute to your upcoming initiatives.\n\n" +
                "Thank you for your time and consideration. I look forward to the possibility of discussing " +
                "how my skills can benefit your organization.\n\n" +
                "Sincerely,\n" + fullName + " (" + uname + ")\n" +
                "Date: " + date;

        return ResponseEntity.ok(Map.of("letter", letter));
    }

    // -------- Idea 4: Admin Analytics (Funnel & Trends) --------
    @GetMapping("/api/admin/analytics")
    public ResponseEntity<Map<String, Object>> getAdminAnalytics() {
        List<Map<String, Object>> allJobs = db.getAllJobs();
        int totalApps = 0;
        int selectedCount = 0;

        Map<String, Integer> skillFrequency = new HashMap<>();

        for (Map<String, Object> job : allJobs) {
            int jobId = (int) job.get("id");
            List<Map<String, Object>> apps = db.getApplicantsForJob(jobId);
            totalApps += apps.size();
            for (Map<String, Object> a : apps) {
                if ("SELECTED".equalsIgnoreCase((String)a.get("status"))) selectedCount++;
            }

            // Extract skill trends from JD
            String jd = (String) job.get("description");
            Set<String> skills = scorer.scoreResume("", "", jd).getMatchedSkills(); // Hack to extract skills from JD string
            for (String s : skills) skillFrequency.put(s, skillFrequency.getOrDefault(s, 0) + 1);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalJobs", allJobs.size());
        stats.put("totalApps", totalApps);
        stats.put("selectedCount", selectedCount);
        stats.put("skillTrends", skillFrequency);

        return ResponseEntity.ok(stats);
    }

    private String extractResumeText(Object rtObj) {
        if (rtObj == null) return "";
        if (rtObj instanceof String) return (String) rtObj;
        if (rtObj instanceof java.sql.Clob) {
            java.sql.Clob clob = (java.sql.Clob) rtObj;
            try {
                return clob.getSubString(1, (int) clob.length());
            } catch (Exception e) {
                return "";
            }
        }
        return rtObj.toString();
    }
}
