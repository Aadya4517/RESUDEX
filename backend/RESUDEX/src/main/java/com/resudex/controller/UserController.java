package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController - manages user profiles.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private DatabaseService db;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String fullName = body.get("fullName");
        String email    = body.get("email");

        boolean success = db.registerUser(username, password, fullName, email);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Registration failed. Username may be taken."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable int id) {
        Map<String, Object> user = db.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable int id, @RequestBody Map<String, String> body) {
        String fullName = body.getOrDefault("full_name", "");
        String email    = body.getOrDefault("email", "");
        String bio      = body.getOrDefault("bio", "");

        db.updateUserProfile(id, fullName, email, bio);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Map<String, String>> addAdminNote(@PathVariable int id, @RequestBody Map<String, String> body) {
        String note = body.get("note");
        if (note != null && !note.trim().isEmpty()) {
            db.addAdminNote(id, note.trim());
            return ResponseEntity.ok(Map.of("message", "Note added successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Note cannot be empty"));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<Map<String, Object>>> getAdminNotes(@PathVariable int id) {
        return ResponseEntity.ok(db.getAdminNotesForUser(id));
    }
}
