package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class AuditLogRepositoryIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private AuditLog log1;
    private AuditLog log2;
    private AuditLog log3;
    private AuditLog log4;
    private AuditLog log5;

    @BeforeEach
    void setUp() {

        auditLogRepository.deleteAll();
        userRepository.deleteAll();


        adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        log1 = createAuditLog(adminUser.getId(), "ISSUE_CREATED", "ISSUE", issueId, 
            "Issue created", Instant.now().minusSeconds(50));
        log2 = createAuditLog(testerUser.getId(), "ISSUE_CREATED", "ISSUE", issueId, 
            "Issue created", Instant.now().minusSeconds(40));
        log3 = createAuditLog(adminUser.getId(), "PROJECT_CREATED", "PROJECT", projectId, 
            "Project created", Instant.now().minusSeconds(30));
        log4 = createAuditLog(developerUser.getId(), "ISSUE_UPDATED", "ISSUE", issueId, 
            "Issue updated", Instant.now().minusSeconds(20));
        log5 = createAuditLog(adminUser.getId(), "ISSUE_STATUS_CHANGED", "ISSUE", issueId, 
            "Status changed", Instant.now().minusSeconds(10));
    }

    private User createUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private AuditLog createAuditLog(UUID actorId, String action, String entityType, 
                                   UUID entityId, String details, Instant timestamp) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setTimestamp(timestamp);
        return auditLogRepository.save(log);
    }

    // Test 1: findByActorUserId(UUID actorId)

    @Test
    void findByActorUserId_ActorHasLogs_ReturnsAllLogs() {

        List<AuditLog> result = auditLogRepository.findByActorUserId(adminUser.getId());

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(log -> 
            log.getActorUserId().equals(adminUser.getId())));
        assertTrue(result.stream().anyMatch(log -> log.getAction().equals("ISSUE_CREATED")));
        assertTrue(result.stream().anyMatch(log -> log.getAction().equals("PROJECT_CREATED")));
        assertTrue(result.stream().anyMatch(log -> log.getAction().equals("ISSUE_STATUS_CHANGED")));
    }

    @Test
    void findByActorUserId_ActorHasNoLogs_ReturnsEmptyList() {

        User newUser = createUser("newuser", "new@example.com", "password123", Role.TESTER);


        List<AuditLog> result = auditLogRepository.findByActorUserId(newUser.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // Test 2: findByAction(String action)

    @Test
    void findByAction_ActionExists_ReturnsAllLogsWithAction() {

        List<AuditLog> result = auditLogRepository.findByAction("ISSUE_CREATED");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.getAction().equals("ISSUE_CREATED")));
        assertTrue(result.stream().anyMatch(log -> log.getActorUserId().equals(adminUser.getId())));
        assertTrue(result.stream().anyMatch(log -> log.getActorUserId().equals(testerUser.getId())));
    }

    @Test
    void findByAction_ActionDoesNotExist_ReturnsEmptyList() {

        List<AuditLog> result = auditLogRepository.findByAction("NON_EXISTENT_ACTION");

     
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByAction_MultipleActions_ReturnsCorrectLogs() {

        List<AuditLog> result = auditLogRepository.findByAction("ISSUE_UPDATED");


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ISSUE_UPDATED", result.get(0).getAction());
        assertEquals(developerUser.getId(), result.get(0).getActorUserId());
    }

  
    // Test 3: findByEntityType(String entityType)

    @Test
    void findByEntityType_EntityTypeExists_ReturnsAllLogsWithType() {

        List<AuditLog> result = auditLogRepository.findByEntityType("ISSUE");

 
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(log -> log.getEntityType().equals("ISSUE")));
    }

    @Test
    void findByEntityType_EntityTypeDoesNotExist_ReturnsEmptyList() {

        List<AuditLog> result = auditLogRepository.findByEntityType("NON_EXISTENT_TYPE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByEntityType_ProjectType_ReturnsProjectLogs() {

        List<AuditLog> result = auditLogRepository.findByEntityType("PROJECT");


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PROJECT", result.get(0).getEntityType());
        assertEquals("PROJECT_CREATED", result.get(0).getAction());
    }


    // Test 4: findAllByOrderByTimestampDesc()


    @Test
    void findAllByOrderByTimestampDesc_LogsExist_ReturnsOrderedLogs() {
        List<AuditLog> result = auditLogRepository.findAllByOrderByTimestampDesc();

        assertNotNull(result);
        assertEquals(5, result.size());
        
        // Verify ordering: log5 (newest) -> log4 -> log3 -> log2 -> log1 (oldest)
        assertEquals("ISSUE_STATUS_CHANGED", result.get(0).getAction()); // log5 - newest
        assertEquals("ISSUE_UPDATED", result.get(1).getAction()); // log4
        assertEquals("PROJECT_CREATED", result.get(2).getAction()); // log3
        assertEquals("ISSUE_CREATED", result.get(3).getAction()); // log2
        assertEquals("ISSUE_CREATED", result.get(4).getAction()); // log1 - oldest
        
        // Verify timestamps are in descending order
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getTimestamp().isAfter(result.get(i + 1).getTimestamp()) ||
                       result.get(i).getTimestamp().equals(result.get(i + 1).getTimestamp()),
                "Logs should be ordered by timestamp descending");
        }
    }

    @Test
    void findAllByOrderByTimestampDesc_NoLogs_ReturnsEmptyList() {

        auditLogRepository.deleteAll();


        List<AuditLog> result = auditLogRepository.findAllByOrderByTimestampDesc();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findAllByOrderByTimestampDesc_SingleLog_ReturnsSingleLog() {

        auditLogRepository.deleteAll();
        AuditLog singleLog = createAuditLog(adminUser.getId(), "TEST_ACTION", "TEST", 
            UUID.randomUUID(), "Test", Instant.now());

        List<AuditLog> result = auditLogRepository.findAllByOrderByTimestampDesc();


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(singleLog.getId(), result.get(0).getId());
    }
}
