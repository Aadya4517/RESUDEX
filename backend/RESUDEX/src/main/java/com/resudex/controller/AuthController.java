package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController - handles user registration, user login, and admin login.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private DatabaseService db;

    // -------- User Register --------
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        boolean success = db.registerUser(username.trim(), password.trim());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Registration successful! Please log in."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken. Try another."));
        }
    }

    // -------- User Login --------
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, Object> user = db.loginUser(username, password);
        if (user != null) {
            return ResponseEntity.ok(Map.of(
                "userId",   user.get("id"),
                "username", user.get("username"),
                "message",  "Login successful"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

    // -------- Admin Login --------
    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if ("admin".equals(username) && "admin123".equals(password)) {
            return ResponseEntity.ok(Map.of("message", "Admin login successful", "role", "ADMIN"));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid admin credentials"));
        }
    }

    // -------- Forgot Password --------
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        
        String token = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        db.setUserResetToken(username, token);
        
        System.out.println("RESET TOKEN FOR " + username + ": " + token);
        return ResponseEntity.ok(Map.of("message", "Reset code generated (check server logs for simulation)"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");
        
        if (token == null || newPassword == null) return ResponseEntity.badRequest().body(Map.of("error", "Token and new password required"));
        
        boolean ok = db.resetPassword(token, newPassword);
        if (ok) return ResponseEntity.ok(Map.of("message", "Password reset successful!"));
        else return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
    }

    // -------- Social Login Simulation --------
    @PostMapping("/social-login")
    public ResponseEntity<Map<String, Object>> socialLogin(@RequestBody Map<String, String> body) {
        String provider = body.get("provider"); // google or microsoft
        String email = body.get("email");
        
        // Mocking: Use a predictable password for mock accounts to ensure persistence
        String dummyPass = "mock_social_user_pass";
        String username = email.split("@")[0];
        
        db.registerUser(username, dummyPass);
        Map<String, Object> user = db.loginUser(username, dummyPass);
        
        return ResponseEntity.ok(Map.of(
            "userId", user.get("id"),
            "username", user.get("username"),
            "message", "Logged in via " + provider
        ));
    }
}
