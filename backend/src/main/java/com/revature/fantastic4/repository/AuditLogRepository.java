package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>
{
    List<AuditLog> findByActorUserId(UUID actorId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findAllByOrderByTimestampDesc();
}
