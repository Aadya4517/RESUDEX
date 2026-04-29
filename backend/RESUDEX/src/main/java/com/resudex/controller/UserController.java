package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// user profile endpoints
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private DatabaseService db;

    // get user profile
    @GetMapping("/see/{uid}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable int uid) {
        Map<String, Object> u = db.getUser(uid);
        if (u != null) {
            return ResponseEntity.ok(u);
        } else {
            return ResponseEntity.status(404).body(Map.of("err", "No such user"));
        }
    }

    // update profile
    @PutMapping("/edit/{uid}")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable int uid, @RequestBody Map<String, String> info) {
        String name = info.getOrDefault("full_name", "");
        String mail = info.getOrDefault("email", "");
        String bio = info.getOrDefault("bio", "");

        db.updateProfile(uid, name, mail, bio);
        return ResponseEntity.ok(Map.of("msg", "Saved"));
    }

    // add note
    @PostMapping("/note/to/{uid}")
    public ResponseEntity<Map<String, String>> addNote(@PathVariable int uid, @RequestBody Map<String, String> data) {
        String n = data.get("note");
        if (n != null && !n.trim().isEmpty()) {
            db.addNote(uid, n.trim());
            return ResponseEntity.ok(Map.of("msg", "Added"));
        }
        return ResponseEntity.badRequest().body(Map.of("err", "Empty note"));
    }

    // list notes
    @GetMapping("/note/of/{uid}")
    public ResponseEntity<List<Map<String, Object>>> getNotes(@PathVariable int uid) {
        return ResponseEntity.ok(db.getNotes(uid));
    }
}
