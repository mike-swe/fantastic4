package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService
{

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository)
    {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog log(UUID actorId, String action, String entityType, UUID entityId, String details)
    {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }

    public List<AuditLog> getLogsByActor(UUID actorId) {
        return auditLogRepository.findByActorUserId(actorId);
    }

    public List<AuditLog> getAllLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}
