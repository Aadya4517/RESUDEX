package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

// auth endpoints
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private DatabaseService db;

    // hash password
    private static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { return raw; }
    }

    // register user
    @PostMapping("/register_usr")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> payload) {
        String usr = payload.get("username");
        String pass = payload.get("password");
        String name = payload.getOrDefault("f_name", "");
        String mail = payload.getOrDefault("email", "");

        if (usr == null || usr.isBlank() || pass == null || pass.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("err", "Username and password needed"));
        }

        boolean ok = db.addUser(usr.trim(), hash(pass.trim()), name, mail);
        if (ok) {
            return ResponseEntity.ok(Map.of("msg", "Reg ok! Log in now."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("err", "User exists. Pick another."));
        }
    }

    // user login
    @PostMapping("/log_in")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String usr = req.get("username");
        String pass = req.get("password");

        Map<String, Object> data = db.loginUser(usr, hash(pass));
        if (data != null) {
            return ResponseEntity.ok(Map.of(
                "uid", data.get("id"),
                "usr", data.get("username"),
                "msg", "Log in ok"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("err", "Bad credentials"));
        }
    }

    // admin login
    @PostMapping("/admin_log_in")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody Map<String, String> params) {
        String usr = params.get("username");
        String pass = params.get("password");

        Map<String, Object> adm = db.loginAdmin(usr, hash(pass));
        if (adm != null) {
            return ResponseEntity.ok(Map.of("msg", "Admin log in ok", "role", "ADMIN", "aid", adm.get("id")));
        } else {
            return ResponseEntity.status(401).body(Map.of("err", "Bad admin credentials"));
        }
    }

    // forgot password
    @PostMapping("/lost_password")
    public ResponseEntity<Map<String, Object>> forgotPw(@RequestBody Map<String, String> body) {
        String usr = body.get("username");
        if (usr == null || usr.isBlank()) return ResponseEntity.badRequest().body(Map.of("err", "User needed"));

        String tok = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        db.saveToken(usr, tok);

        System.out.println("RESET TOKEN FOR " + usr + ": " + tok);
        return ResponseEntity.ok(Map.of("msg", "Check logs for code"));
    }

    // reset password
    @PostMapping("/reset_now")
    public ResponseEntity<Map<String, Object>> resetPw(@RequestBody Map<String, String> input) {
        String tok = input.get("token");
        String pass = input.get("password");

        if (tok == null || pass == null) return ResponseEntity.badRequest().body(Map.of("err", "Need token and pass"));

        boolean done = db.resetPw(tok, hash(pass));
        if (done) return ResponseEntity.ok(Map.of("msg", "Pass reset ok!"));
        else return ResponseEntity.badRequest().body(Map.of("err", "Invalid token"));
    }

    // social sign in
    @PostMapping("/social_sign_in")
    public ResponseEntity<Map<String, Object>> socialLogin(@RequestBody Map<String, String> data) {
        String type = data.get("provider");
        String mail = data.get("email");

        String pass = "mock_social_pass";
        String usr = mail.split("@")[0];

        db.addUser(usr, hash(pass), usr, mail);
        Map<String, Object> u = db.loginUser(usr, hash(pass));

        return ResponseEntity.ok(Map.of(
            "uid", u.get("id"),
            "usr", u.get("username"),
            "msg", "In via " + type
        ));
    }
}
