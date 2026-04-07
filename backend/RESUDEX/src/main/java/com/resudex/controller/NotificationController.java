package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private DatabaseService db;

    @GetMapping("/{userId}")
    public List<Map<String, Object>> getNotifications(@PathVariable int userId) {
        return db.getUnreadNotifications(userId);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable int id) {
        db.markNotificationRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }
}
