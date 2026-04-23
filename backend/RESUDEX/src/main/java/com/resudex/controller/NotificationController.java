package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private DatabaseService app_db;

    @GetMapping("/new_for/{uid}")
    public List<Map<String, Object>> list_new(@PathVariable int uid) {
        return app_db.get_unread(uid);
    }

    @PostMapping("/dismiss/{nid}")
    public ResponseEntity<Map<String, Object>> done_notif(@PathVariable int nid) {
        app_db.seen_notif(nid);
        return ResponseEntity.ok(Map.of("message", "Dismissed"));
    }
}
