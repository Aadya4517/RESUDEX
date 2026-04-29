package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// notification endpoints
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private DatabaseService db;

    // get unread notifications
    @GetMapping("/new_for/{uid}")
    public List<Map<String, Object>> getNew(@PathVariable int uid) {
        return db.getUnread(uid);
    }

    // dismiss notification
    @PostMapping("/dismiss/{nid}")
    public ResponseEntity<Map<String, Object>> dismiss(@PathVariable int nid) {
        db.markRead(nid);
        return ResponseEntity.ok(Map.of("message", "Dismissed"));
    }
}
