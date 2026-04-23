package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles user profile stuff.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private DatabaseService app_db;

    @GetMapping("/see/{uid}")
    public ResponseEntity<Map<String, Object>> see_me(@PathVariable int uid) {
        Map<String, Object> u = app_db.get_usr_by_id(uid);
        if (u != null) {
            return ResponseEntity.ok(u);
        } else {
            return ResponseEntity.status(404).body(Map.of("err", "No such user"));
        }
    }

    @PutMapping("/edit/{uid}")
    public ResponseEntity<Map<String, Object>> edit_me(@PathVariable int uid, @RequestBody Map<String, String> info) {
        String name = info.getOrDefault("full_name", "");
        String mail = info.getOrDefault("email", "");
        String b = info.getOrDefault("bio", "");

        app_db.update_profile(uid, name, mail, b);
        return ResponseEntity.ok(Map.of("msg", "Saved"));
    }

    @PostMapping("/note/to/{uid}")
    public ResponseEntity<Map<String, String>> put_note(@PathVariable int uid, @RequestBody Map<String, String> data) {
        String n = data.get("note");
        if (n != null && !n.trim().isEmpty()) {
            app_db.add_note(uid, n.trim());
            return ResponseEntity.ok(Map.of("msg", "Added"));
        }
        return ResponseEntity.badRequest().body(Map.of("err", "Empty note"));
    }

    @GetMapping("/note/of/{uid}")
    public ResponseEntity<List<Map<String, Object>>> list_notes(@PathVariable int uid) {
        return ResponseEntity.ok(app_db.get_notes(uid));
    }
}
