<<<<<<< HEAD
//package com.revature.fantastic4.service;
//
//import com.revature.fantastic4.entity.AuditLog;
//import com.revature.fantastic4.repository.AuditLogRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AuditService
//{
//
//    private final AuditLogRepository auditLogRepository;
//
//    public AuditService(AuditLogRepository auditLogRepository)
//    {
//        this.auditLogRepository = auditLogRepository;
//    }
//
//    public AuditLog log(Long actorId, String action, String entityType, Long entityId, String details)
//    {
//        AuditLog log = new AuditLog();
//        log.setActorUserId(actorId);
//        log.setAction(action);
//        log.setEntityType(entityType);
//        log.setEntityId(entityId);
//        log.setDetails(details);
//        return auditLogRepository.save(log);
//    }
//}
=======
package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService 
{

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) 
    {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog log(Long actorId, String action, String entityType, Long entityId, String details) 
    {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogsByActor(Long actorId) {
        List<AuditLog> logsByActor = auditLogRepository.findByActorUserId(actorId);
        return logsByActor;
    }

    public List<AuditLog> getAllLogsByAction(String action) {
        List<AuditLog> logsByAction = auditLogRepository.findByAction(action);
        return logsByAction;
    }

    public List<AuditLog> getAllLogsByEntityType(String entityType) {
        List<AuditLog> logsByEntityType = auditLogRepository.findByEntityType(entityType);
        return logsByEntityType;
    }
}
>>>>>>> c55563e (Implemented repository methods from Project and AuditLog)
