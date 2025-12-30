package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> 
{

}
