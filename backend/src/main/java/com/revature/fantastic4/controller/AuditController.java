package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditController 
{

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) 
    {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/audit")
    public List<AuditLog> getAllLogs() 
    {
        return auditLogRepository.findAll();
    }
}
