package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.service.AuditService;
import com.revature.fantastic4.service.UserService;
import com.revature.fantastic4.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@AllArgsConstructor
public class AuditController 
{

    private final AuditService auditService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    private void validateAdminRole(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only Admin users can access audit logs");
        }
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader) 
    {
        validateAdminRole(authHeader);
        List<AuditLog> logs = auditService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(
            @PathVariable String entityType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) 
    {
        validateAdminRole(authHeader);
        List<AuditLog> logs = auditService.getLogsByEntityType(entityType);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<List<AuditLog>> getLogsByActor(
            @PathVariable UUID actorId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) 
    {
        validateAdminRole(authHeader);
        List<AuditLog> logs = auditService.getLogsByActor(actorId);
        return ResponseEntity.ok(logs);
    }
}
