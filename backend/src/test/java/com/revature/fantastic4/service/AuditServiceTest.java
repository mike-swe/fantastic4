package com.revature.fantastic4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.fantastic4.entity.AuditLog;
import com.revature.fantastic4.repository.AuditLogRepository;
import org.junit.jupiter.params.ParameterizedTest;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private UUID actorId;
    private UUID entityId;
    private UUID anotherActorId;
    private UUID anotherEntityId;
    private AuditLog testLog1;
    private AuditLog testLog2;
    private AuditLog testLog3;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
        entityId = UUID.randomUUID();
        anotherActorId = UUID.randomUUID();
        anotherEntityId = UUID.randomUUID();

        // Issue created 
        testLog1 = new AuditLog();
        testLog1.setId(1L);
        testLog1.setActorUserId(actorId);
        testLog1.setAction("ISSUE_CREATED");
        testLog1.setEntityType("ISSUE");
        testLog1.setEntityId(entityId);
        testLog1.setTimestamp(Instant.now().minusSeconds(30));
        testLog1.setDetails("Issue created with title: Test Issue");
        
        // Issue Updated
        testLog2 = new AuditLog();
        testLog2.setId(2L);
        testLog2.setActorUserId(actorId);
        testLog2.setAction("ISSUE_UPDATED");
        testLog2.setEntityType("ISSUE");
        testLog2.setEntityId(entityId);
        testLog2.setTimestamp(Instant.now().minusSeconds(20));
        testLog2.setDetails("Issue updated: Status changed");

        // Project Created
        testLog3 = new AuditLog();
        testLog3.setId(3L);
        testLog3.setActorUserId(anotherActorId);
        testLog3.setAction("PROJECT_CREATED");
        testLog3.setEntityType("PROJECT");
        testLog3.setEntityId(anotherEntityId);
        testLog3.setTimestamp(Instant.now().minusSeconds(10));
        testLog3.setDetails("Project created with name: Test Project");
    }

    // ========== log() Tests ==========

    @Test
    void log_CreatesAuditLogWithAllFields_Success(UUID testActorId, String action, String entityType, UUID testEntityId, String details, String expectedMessage) {
        testActorId = UUID.randomUUID();
        action = "ISSUE_CREATED";
        entityType = "ISSUE";
        testEntityId = UUID.randomUUID();
        details = "Issue created with title: New Issue";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        AuditLog result = auditService.log(testActorId, action, entityType, testEntityId, details);

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals(testActorId, capturedLog.getActorUserId());
        assertEquals(action, capturedLog.getAction());
        assertEquals(entityType, capturedLog.getEntityType());
        assertEquals(testEntityId, capturedLog.getEntityId());
        assertEquals(details, capturedLog.getDetails());
        assertNotNull(capturedLog.getTimestamp());

        assertNotNull(result);
        assertEquals(testActorId, result.getActorUserId());
        assertEquals(action, result.getAction());
        assertEquals(entityType, result.getEntityType());
        assertEquals(testEntityId, result.getEntityId());
        assertEquals(details, result.getDetails());
    }

    @ParameterizedTest
    @MethodSource("invalidAuditLogInputs")
    void log_CreatesAuditLogWithAllFields_Failed(UUID testActorId, String action, String entityType, UUID testEntityId, String details, String expectedMessage) {
         IllegalArgumentException exception = assertThrows(
                 IllegalArgumentException.class,
                 () -> auditService.log(testActorId, action, entityType, testEntityId, details)
         );

         assertEquals(expectedMessage, exception.getMessage());

         verifyNoInteractions(auditLogRepository);
    }

    private static Stream<Arguments> invalidAuditLogInputs() {
        UUID sampleId = UUID.randomUUID();

        return Stream.of(
                Arguments.of(null, "ISSUE_CREATED", "ISSUE", sampleId,
                        "Issue created", "actorId cannot be null"),

                Arguments.of(sampleId, null, "ISSUE", sampleId,
                        "Issue created", "action cannot be null"),

                Arguments.of(sampleId, "ISSUE_CREATED", "ISSUE", null,
                        "Issue created", "entityId cannot be null"),

                Arguments.of(sampleId, "ISSUE_CREATED", "ISSUE", sampleId,
                        null, "details cannot be null")
        );
    }
    @Test
    void log_ReturnsSavedAuditLog_Success() {
        UUID testActorId = UUID.randomUUID();
        String action = "PROJECT_UPDATED";
        String entityType = "PROJECT";
        UUID testEntityId = UUID.randomUUID();
        String details = "Project updated: Status changed to ACTIVE";

        AuditLog savedLog = new AuditLog();
        savedLog.setId(100L);
        savedLog.setActorUserId(testActorId);
        savedLog.setAction(action);
        savedLog.setEntityType(entityType);
        savedLog.setEntityId(testEntityId);
        savedLog.setDetails(details);
        savedLog.setTimestamp(Instant.now());

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        AuditLog result = auditService.log(testActorId, action, entityType, testEntityId, details);

        assertNotNull(result);
        assertEquals(savedLog.getId(), result.getId());
        assertEquals(savedLog.getActorUserId(), result.getActorUserId());
        assertEquals(savedLog.getAction(), result.getAction());
        assertEquals(savedLog.getEntityType(), result.getEntityType());
        assertEquals(savedLog.getEntityId(), result.getEntityId());
        assertEquals(savedLog.getDetails(), result.getDetails());
        assertEquals(savedLog.getTimestamp(), result.getTimestamp());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void log_HandlesNullValues_Success() {
        UUID testActorId = UUID.randomUUID();
        String action = "ISSUE_DELETED";
        String entityType = "ISSUE";
        UUID testEntityId = null;
        String details = null;

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        AuditLog result = auditService.log(testActorId, action, entityType, testEntityId, details);

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals(testActorId, capturedLog.getActorUserId());
        assertEquals(action, capturedLog.getAction());
        assertEquals(entityType, capturedLog.getEntityType());
        assertNull(capturedLog.getEntityId());
        assertNull(capturedLog.getDetails());

        assertNotNull(result);
        assertNull(result.getEntityId());
        assertNull(result.getDetails());
    }

    // ========== getAllLogs() Tests ==========

    @Test
    void getAllLogs_ReturnsAllLogsOrderedByTimestampDesc_Success() {
        List<AuditLog> logs = List.of(testLog3, testLog2, testLog1); 
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(logs);

        List<AuditLog> result = auditService.getAllLogs();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(testLog3, result.get(0));
        assertEquals(testLog2, result.get(1));
        assertEquals(testLog1, result.get(2));
        
        // Verify ordering: each log should have timestamp >= next log
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()) ||
                   result.get(0).getTimestamp().equals(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isAfter(result.get(2).getTimestamp()) ||
                   result.get(1).getTimestamp().equals(result.get(2).getTimestamp()));
        
        verify(auditLogRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void getAllLogs_ReturnsEmptyListIfNoLogsExist_Success() {
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of());

        List<AuditLog> result = auditService.getAllLogs();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(auditLogRepository).findAllByOrderByTimestampDesc();
    }

    // ========== getLogsByEntityType() Tests ==========

    @Test
    void getLogsByEntityType_ReturnsLogsForSpecificEntityType_Success() {
        String entityType = "ISSUE";
        List<AuditLog> issueLogs = List.of(testLog2, testLog1); // Both are ISSUE type
        when(auditLogRepository.findByEntityType(entityType)).thenReturn(issueLogs);

        List<AuditLog> result = auditService.getLogsByEntityType(entityType);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testLog2, result.get(0));
        assertEquals(testLog1, result.get(1));
        
        assertTrue(result.stream().allMatch(log -> entityType.equals(log.getEntityType())));
        
        verify(auditLogRepository).findByEntityType(entityType);
    }

    @Test
    void getLogsByEntityType_ReturnsEmptyListIfNoLogsForEntityType_Success() {
        String entityType = "USER";
        when(auditLogRepository.findByEntityType(entityType)).thenReturn(List.of());

        List<AuditLog> result = auditService.getLogsByEntityType(entityType);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(auditLogRepository).findByEntityType(entityType);
    }

    // ========== getLogsByActor() Tests ==========

    @Test
    void getLogsByActor_ReturnsLogsForSpecificActor_Success() {
        List<AuditLog> actorLogs = List.of(testLog2, testLog1); // Both have same actorId
        when(auditLogRepository.findByActorUserId(actorId)).thenReturn(actorLogs);

        List<AuditLog> result = auditService.getLogsByActor(actorId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testLog2, result.get(0));
        assertEquals(testLog1, result.get(1));
        
        // Verify all returned logs have the correct actor ID
        assertTrue(result.stream().allMatch(log -> actorId.equals(log.getActorUserId())));
        
        verify(auditLogRepository).findByActorUserId(actorId);
    }

    @Test
    void getLogsByActor_ReturnsEmptyListIfActorHasNoLogs_Success() {
        UUID nonExistentActorId = UUID.randomUUID();
        when(auditLogRepository.findByActorUserId(nonExistentActorId)).thenReturn(List.of());

        List<AuditLog> result = auditService.getLogsByActor(nonExistentActorId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(auditLogRepository).findByActorUserId(nonExistentActorId);
    }

    // ========== getAllLogsByAction() Tests ==========

    @Test
    void getAllLogsByAction_ReturnsLogsForSpecificAction_Success() {
        String action = "ISSUE_CREATED";
        List<AuditLog> actionLogs = List.of(testLog1); // Only testLog1 has ISSUE_CREATED action
        when(auditLogRepository.findByAction(action)).thenReturn(actionLogs);

        List<AuditLog> result = auditService.getAllLogsByAction(action);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog1, result.get(0));
        
        assertTrue(result.stream().allMatch(log -> action.equals(log.getAction())));
        
        verify(auditLogRepository).findByAction(action);
    }

    @Test
    void getAllLogsByAction_ReturnsEmptyListIfNoLogsForAction_Success() {
        String action = "ISSUE_DELETED";
        when(auditLogRepository.findByAction(action)).thenReturn(List.of());

        List<AuditLog> result = auditService.getAllLogsByAction(action);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(auditLogRepository).findByAction(action);
    }
}
