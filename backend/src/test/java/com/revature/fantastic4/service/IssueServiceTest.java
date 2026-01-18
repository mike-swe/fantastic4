package com.revature.fantastic4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.IssueHistory;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ChangeType;
import com.revature.fantastic4.enums.IssueFieldName;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.IssueHistoryRepository;
import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;

@ExtendWith(MockitoExtension.class)
public class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private IssueHistoryRepository issueHistoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private IssueService issueService;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;
    private Issue testIssue;
    private UUID projectId;
    private UUID issueId;
    private UUID adminId;
    private UUID testerId;
    private UUID developerId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setUsername("admin");
        adminUser.setPassword("password123");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        testerId = UUID.randomUUID();
        testerUser = new User();
        testerUser.setId(testerId);
        testerUser.setUsername("tester");
        testerUser.setPassword("password123");
        testerUser.setEmail("tester@example.com");
        testerUser.setRole(Role.TESTER);

        developerId = UUID.randomUUID();
        developerUser = new User();
        developerUser.setId(developerId);
        developerUser.setUsername("developer");
        developerUser.setPassword("password123");
        developerUser.setEmail("developer@example.com");
        developerUser.setRole(Role.DEVELOPER);

        projectId = UUID.randomUUID();
        testProject = new Project();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());

        issueId = UUID.randomUUID();
        testIssue = new Issue();
        testIssue.setId(issueId);
        testIssue.setTitle("Test Issue");
        testIssue.setDescription("Test Description");
        testIssue.setStatus(IssueStatus.OPEN);
        testIssue.setSeverity(Severity.MEDIUM);
        testIssue.setPriority(Priority.HIGH);
        testIssue.setProject(testProject);
        testIssue.setCreatedBy(testerUser);
        testIssue.setCreatedAt(Instant.now());
        testIssue.setUpdatedAt(Instant.now());
    }

    // ========== createIssue() Tests ==========

    @Test
    void createIssue_TesterCanCreateIssue_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(severity, result.getSeverity());
        assertEquals(priority, result.getPriority());
        assertEquals(IssueStatus.OPEN, result.getStatus());
        assertEquals(testProject, result.getProject());
        assertEquals(testerUser, result.getCreatedBy());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(projectService).getProjectById(projectId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_CREATED"), eq("ISSUE"), eq(result.getId()), anyString());
    }

    @Test
    void createIssue_DeveloperCannotCreateIssue_ThrowsException() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, description, severity, priority, projectId, developerUser);
        });

        assertEquals("Only Testers can create issues", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_AdminCannotCreateIssue_ThrowsException() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, description, severity, priority, projectId, adminUser);
        });

        assertEquals("Only Testers can create issues", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueTitleCannotBeNull_ThrowsException() {
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(null, description, severity, priority, projectId, testerUser);
        });

        assertEquals("Issue title cannot be null or empty", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueTitleCannotBeEmpty_ThrowsException() {
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue("", description, severity, priority, projectId, testerUser);
        });

        assertEquals("Issue title cannot be null or empty", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueTitleCannotBeWhitespaceOnly_ThrowsException() {
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue("   ", description, severity, priority, projectId, testerUser);
        });

        assertEquals("Issue title cannot be null or empty", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueDescriptionCannotBeNull_ThrowsException() {
        String title = "New Issue";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, null, severity, priority, projectId, testerUser);
        });

        assertEquals("Issue description cannot be null or empty", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueDescriptionCannotBeEmpty_ThrowsException() {
        String title = "New Issue";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, "", severity, priority, projectId, testerUser);
        });

        assertEquals("Issue description cannot be null or empty", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueSeverityCannotBeNull_ThrowsException() {
        String title = "New Issue";
        String description = "Issue Description";
        Priority priority = Priority.CRITICAL;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, description, null, priority, projectId, testerUser);
        });

        assertEquals("Issue severity cannot be null", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssuePriorityCannotBeNull_ThrowsException() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, description, severity, null, projectId, testerUser);
        });

        assertEquals("Issue priority cannot be null", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_TesterMustBeAssignedToProject_ThrowsException() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(title, description, severity, priority, projectId, testerUser);
        });

        assertEquals("User is not assigned to this project", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createIssue_IssueStatusDefaultsToOpen_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        assertEquals(IssueStatus.OPEN, result.getStatus());
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void createIssue_IssueCreatedByIsSetToTester_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        assertEquals(testerUser, result.getCreatedBy());
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void createIssue_IssueCreatedAtAndUpdatedAtAreSet_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;
        Instant beforeCreation = Instant.now();

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)) || 
                   result.getCreatedAt().equals(beforeCreation));
        assertTrue(result.getUpdatedAt().isAfter(beforeCreation.minusSeconds(1)) || 
                   result.getUpdatedAt().equals(beforeCreation));
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void createIssue_IssueHistoryRecordIsCreated_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        ArgumentCaptor<IssueHistory> historyCaptor = ArgumentCaptor.forClass(IssueHistory.class);
        verify(issueHistoryRepository).save(historyCaptor.capture());
        
        IssueHistory history = historyCaptor.getValue();
        assertNotNull(history);
        assertEquals(result, history.getIssue());
        assertEquals(testerUser, history.getChangedByUser());
        assertEquals(ChangeType.CREATED, history.getChangeType());
        assertNotNull(history.getChangedAt());
    }

    @Test
    void createIssue_AuditLogIsCreated_Success() {
        String title = "New Issue";
        String description = "Issue Description";
        Severity severity = Severity.HIGH;
        Priority priority = Priority.CRITICAL;

        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(UUID.randomUUID());
            return issue;
        });
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(title, description, severity, priority, projectId, testerUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(testerId), eq("ISSUE_CREATED"), eq("ISSUE"), eq(result.getId()), detailsCaptor.capture());
        
        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Issue created"));
        assertTrue(details.contains(title));
        assertTrue(details.contains(severity.toString()));
        assertTrue(details.contains(priority.toString()));
    }

    // ========== updateIssueStatus() Tests ==========

    @Test
    void updateIssueStatus_DeveloperCanChangeStatusToInProgress_Success() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, developerUser);

        assertEquals(newStatus, result.getStatus());
        assertEquals(developerUser, result.getAssignedTo());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository, times(2)).save(any(IssueHistory.class));
        verify(auditService).log(eq(developerId), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssueStatus_DeveloperCanChangeStatusToResolved_Success() {
        IssueStatus newStatus = IssueStatus.RESOLVED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, developerUser);

        assertEquals(newStatus, result.getStatus());
        assertEquals(developerUser, result.getAssignedTo());
        assertNotNull(result.getResolvedAt());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(developerId), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssueStatus_DeveloperCannotChangeStatusToOpen_ThrowsException() {
        IssueStatus newStatus = IssueStatus.OPEN;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, developerUser);
        });

        assertEquals("Developers can only set status to IN_PROGRESS or RESOLVED", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_DeveloperCannotChangeStatusToClosed_ThrowsException() {
        IssueStatus newStatus = IssueStatus.CLOSED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, developerUser);
        });

        assertEquals("Developers can only set status to IN_PROGRESS or RESOLVED", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_TesterCanChangeStatusToClosed_Success() {
        IssueStatus newStatus = IssueStatus.CLOSED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, testerUser);

        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssueStatus_TesterCanChangeStatusToOpen_Success() {
        testIssue.setStatus(IssueStatus.CLOSED);
        IssueStatus newStatus = IssueStatus.OPEN;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, testerUser);

        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssueStatus_TesterCannotChangeStatusToInProgress_ThrowsException() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, testerUser);
        });

        assertEquals("Testers can only set status to CLOSED or OPEN", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_TesterCannotChangeStatusToResolved_ThrowsException() {
        IssueStatus newStatus = IssueStatus.RESOLVED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, testerUser);
        });

        assertEquals("Testers can only set status to CLOSED or OPEN", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_AdminCannotChangeStatus_ThrowsException() {
        IssueStatus newStatus = IssueStatus.CLOSED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, adminUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, adminUser);
        });

        assertEquals("Only Testers and Developers can update issue status", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, adminUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_UserMustBeAssignedToProject_ThrowsException() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssueStatus(issueId, newStatus, developerUser);
        });

        assertEquals("User is not assigned to this project", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssueStatus_IssueAssignedToIsSetWhenDeveloperMovesToInProgress_Success() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, developerUser);

        assertEquals(developerUser, result.getAssignedTo());
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void updateIssueStatus_IssueAssignedToIsSetWhenDeveloperMovesToResolved_Success() {
        IssueStatus newStatus = IssueStatus.RESOLVED;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, developerUser);

        assertEquals(developerUser, result.getAssignedTo());
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void updateIssueStatus_IssueResolvedAtIsSetWhenStatusIsResolved_Success() {
        IssueStatus newStatus = IssueStatus.RESOLVED;
        Instant beforeUpdate = Instant.now();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssueStatus(issueId, newStatus, developerUser);

        assertNotNull(result.getResolvedAt());
        assertTrue(result.getResolvedAt().isAfter(beforeUpdate.minusSeconds(1)) || 
                   result.getResolvedAt().equals(beforeUpdate));
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void updateIssueStatus_IssueHistoryRecordIsCreatedForStatusChange_Success() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssueStatus(issueId, newStatus, developerUser);

        ArgumentCaptor<IssueHistory> historyCaptor = ArgumentCaptor.forClass(IssueHistory.class);
        verify(issueHistoryRepository, org.mockito.Mockito.atLeastOnce()).save(historyCaptor.capture());
        
        List<IssueHistory> histories = historyCaptor.getAllValues();
        boolean foundStatusChange = histories.stream()
            .anyMatch(h -> h.getChangeType() == ChangeType.STATUS_CHANGE && 
                          h.getFieldName() == IssueFieldName.STATUS);
        assertTrue(foundStatusChange);
    }

    @Test
    void updateIssueStatus_AuditLogIsCreatedForStatusChange_Success() {
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssueStatus(issueId, newStatus, developerUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(developerId), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(issueId), detailsCaptor.capture());
        
        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Status changed"));
        assertTrue(details.contains(IssueStatus.OPEN.toString()));
        assertTrue(details.contains(newStatus.toString()));
    }

    @Test
    void updateIssueStatus_NoHistoryAuditWhenStatusDoesntChange_Success() {
        IssueStatus sameStatus = IssueStatus.OPEN;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssueStatus(issueId, sameStatus, testerUser);

        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository, never()).save(any(IssueHistory.class));
        verify(auditService, never()).log(any(), eq("ISSUE_STATUS_CHANGED"), anyString(), any(), anyString());
    }

    // ========== updateIssue() Tests ==========

    @Test
    void updateIssue_CanUpdateIssueTitle_Success() {
        String newTitle = "Updated Title";

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, newTitle, null, null, null, testerUser);

        assertEquals(newTitle, result.getTitle());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_CanUpdateIssueDescription_Success() {
        String newDescription = "Updated Description";

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, null, newDescription, null, null, testerUser);

        assertEquals(newDescription, result.getDescription());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_CanUpdateIssueSeverity_Success() {
        Severity newSeverity = Severity.CRITICAL;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, null, null, newSeverity, null, testerUser);

        assertEquals(newSeverity, result.getSeverity());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_CanUpdateIssuePriority_Success() {
        Priority newPriority = Priority.LOW;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, null, null, null, newPriority, testerUser);

        assertEquals(newPriority, result.getPriority());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_CanUpdateMultipleFieldsAtOnce_Success() {
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        Severity newSeverity = Severity.CRITICAL;
        Priority newPriority = Priority.LOW;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, newTitle, newDescription, newSeverity, newPriority, testerUser);

        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newSeverity, result.getSeverity());
        assertEquals(newPriority, result.getPriority());
        assertNotNull(result.getUpdatedAt());
        
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository, org.mockito.Mockito.atLeast(4)).save(any(IssueHistory.class));
        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_TitleCannotBeEmptyWhenUpdating_ThrowsException() {
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssue(issueId, "", null, null, null, testerUser);
        });

        assertEquals("Issue title cannot be empty", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssue_DescriptionCannotBeEmptyWhenUpdating_ThrowsException() {
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssue(issueId, null, "", null, null, testerUser);
        });

        assertEquals("Issue description cannot be empty", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssue_UserMustBeAssignedToProject_ThrowsException() {
        String newTitle = "Updated Title";

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssue(issueId, newTitle, null, null, null, testerUser);
        });

        assertEquals("User is not assigned to this project", exception.getMessage());
        verify(issueRepository).findById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(issueRepository, never()).save(any(Issue.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateIssue_IssueUpdatedAtIsSet_Success() {
        String newTitle = "Updated Title";
        Instant beforeUpdate = Instant.now();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateIssue(issueId, newTitle, null, null, null, testerUser);

        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)) || 
                   result.getUpdatedAt().equals(beforeUpdate));
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void updateIssue_IssueHistoryRecordsCreatedForEachChangedField_Success() {
        String newTitle = "Updated Title";
        Severity newSeverity = Severity.CRITICAL;

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssue(issueId, newTitle, null, newSeverity, null, testerUser);

        ArgumentCaptor<IssueHistory> historyCaptor = ArgumentCaptor.forClass(IssueHistory.class);
        verify(issueHistoryRepository, org.mockito.Mockito.atLeast(2)).save(historyCaptor.capture());
        
        List<IssueHistory> histories = historyCaptor.getAllValues();
        assertTrue(histories.stream().anyMatch(h -> h.getFieldName() == IssueFieldName.TITLE));
        assertTrue(histories.stream().anyMatch(h -> h.getFieldName() == IssueFieldName.SEVERITY));
    }

    @Test
    void updateIssue_AuditLogCreatedOnlyWhenChangesOccur_Success() {
        String newTitle = "Updated Title";

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssue(issueId, newTitle, null, null, null, testerUser);

        verify(auditService).log(eq(testerId), eq("ISSUE_UPDATED"), eq("ISSUE"), eq(issueId), anyString());
    }

    @Test
    void updateIssue_NoHistoryAuditWhenNoChangesMade_Success() {
        String sameTitle = testIssue.getTitle();
        String sameDescription = testIssue.getDescription();
        Severity sameSeverity = testIssue.getSeverity();
        Priority samePriority = testIssue.getPriority();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueService.updateIssue(issueId, sameTitle, sameDescription, sameSeverity, samePriority, testerUser);

        verify(issueRepository).save(any(Issue.class));
        verify(issueHistoryRepository, never()).save(any(IssueHistory.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    // ========== getIssueById() Tests ==========

    @Test
    void getIssueById_ReturnsIssueWhenFound_Success() {
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));

        Issue result = issueService.getIssueById(issueId);

        assertNotNull(result);
        assertEquals(testIssue, result);
        assertEquals(testIssue.getId(), result.getId());
        assertEquals(testIssue.getTitle(), result.getTitle());
        verify(issueRepository).findById(issueId);
    }

    @Test
    void getIssueById_ThrowsExceptionWhenIssueNotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(issueRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.getIssueById(nonExistentId);
        });

        assertEquals("Issue with ID " + nonExistentId + " not found", exception.getMessage());
        verify(issueRepository).findById(nonExistentId);
    }

    // ========== getAllIssues() Tests ==========

    @Test
    void getAllIssues_ReturnsAllIssues_Success() {
        Issue issue1 = new Issue();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Issue 1");
        
        Issue issue2 = new Issue();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Issue 2");
        
        Issue issue3 = new Issue();
        issue3.setId(UUID.randomUUID());
        issue3.setTitle("Issue 3");
        
        List<Issue> issues = List.of(issue1, issue2, issue3);
        when(issueRepository.findAll()).thenReturn(issues);

        List<Issue> result = issueService.getAllIssues();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(issue1, result.get(0));
        assertEquals(issue2, result.get(1));
        assertEquals(issue3, result.get(2));
        verify(issueRepository).findAll();
    }

    @Test
    void getAllIssues_ReturnsEmptyListIfNoIssuesExist_Success() {
        when(issueRepository.findAll()).thenReturn(List.of());

        List<Issue> result = issueService.getAllIssues();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(issueRepository).findAll();
    }

    // ========== getIssuesByProject() Tests ==========

    @Test
    void getIssuesByProject_ReturnsIssuesForAProject_Success() {
        Issue issue1 = new Issue();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Issue 1");
        issue1.setProject(testProject);
        
        Issue issue2 = new Issue();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Issue 2");
        issue2.setProject(testProject);
        
        List<Issue> issues = List.of(issue1, issue2);
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(issueRepository.findByProject(testProject)).thenReturn(issues);

        List<Issue> result = issueService.getIssuesByProject(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1, result.get(0));
        assertEquals(issue2, result.get(1));
        verify(projectService).getProjectById(projectId);
        verify(issueRepository).findByProject(testProject);
    }

    @Test
    void getIssuesByProject_ReturnsEmptyListIfProjectHasNoIssues_Success() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(issueRepository.findByProject(testProject)).thenReturn(List.of());

        List<Issue> result = issueService.getIssuesByProject(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(projectService).getProjectById(projectId);
        verify(issueRepository).findByProject(testProject);
    }

    @Test
    void getIssuesByProject_NonExistentProjectThrowsException_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(projectService.getProjectById(nonExistentId))
            .thenThrow(new IllegalArgumentException("Project with ID " + nonExistentId + " not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.getIssuesByProject(nonExistentId);
        });

        assertEquals("Project with ID " + nonExistentId + " not found", exception.getMessage());
        verify(projectService).getProjectById(nonExistentId);
        verify(issueRepository, never()).findByProject(any(Project.class));
    }

    // ========== getIssuesByUser() Tests ==========

    @Test
    void getIssuesByUser_ReturnsIssuesCreatedByAUser_Success() {
        Issue issue1 = new Issue();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Issue 1");
        issue1.setCreatedBy(testerUser);
        
        Issue issue2 = new Issue();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Issue 2");
        issue2.setCreatedBy(testerUser);
        
        List<Issue> issues = List.of(issue1, issue2);
        when(userService.getUserById(testerId)).thenReturn(testerUser);
        when(issueRepository.findByCreatedBy(testerUser)).thenReturn(issues);

        List<Issue> result = issueService.getIssuesByUser(testerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1, result.get(0));
        assertEquals(issue2, result.get(1));
        verify(userService).getUserById(testerId);
        verify(issueRepository).findByCreatedBy(testerUser);
    }

    @Test
    void getIssuesByUser_ReturnsEmptyListIfUserCreatedNoIssues_Success() {
        when(userService.getUserById(testerId)).thenReturn(testerUser);
        when(issueRepository.findByCreatedBy(testerUser)).thenReturn(List.of());

        List<Issue> result = issueService.getIssuesByUser(testerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(userService).getUserById(testerId);
        verify(issueRepository).findByCreatedBy(testerUser);
    }

    @Test
    void getIssuesByUser_NonExistentUserThrowsException_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId))
            .thenThrow(new IllegalArgumentException("User with ID " + nonExistentId + " not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.getIssuesByUser(nonExistentId);
        });

        assertEquals("User with ID " + nonExistentId + " not found", exception.getMessage());
        verify(userService).getUserById(nonExistentId);
        verify(issueRepository, never()).findByCreatedBy(any(User.class));
    }

    // ========== getIssuesAssignedToDeveloper() Tests ==========

    @Test
    void getIssuesAssignedToDeveloper_ReturnsIssuesForDevelopersProjects_Success() {
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");
        
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        
        ProjectAssignment assignment1 = new ProjectAssignment();
        assignment1.setId(UUID.randomUUID());
        assignment1.setProject(project1);
        assignment1.setUser(developerUser);
        
        ProjectAssignment assignment2 = new ProjectAssignment();
        assignment2.setId(UUID.randomUUID());
        assignment2.setProject(project2);
        assignment2.setUser(developerUser);
        
        Issue issue1 = new Issue();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Issue 1");
        issue1.setProject(project1);
        
        Issue issue2 = new Issue();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Issue 2");
        issue2.setProject(project2);
        
        when(userService.getUserById(developerId)).thenReturn(developerUser);
        when(projectAssignmentRepository.findByUser(developerUser))
            .thenReturn(List.of(assignment1, assignment2));
        when(issueRepository.findByProject(project1)).thenReturn(List.of(issue1));
        when(issueRepository.findByProject(project2)).thenReturn(List.of(issue2));

        List<Issue> result = issueService.getIssuesAssignedToDeveloper(developerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService).getUserById(developerId);
        verify(projectAssignmentRepository).findByUser(developerUser);
    }

    @Test
    void getIssuesAssignedToDeveloper_ThrowsExceptionIfUserIsNotADeveloper_ThrowsException() {
        when(userService.getUserById(testerId)).thenReturn(testerUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.getIssuesAssignedToDeveloper(testerId);
        });

        assertEquals("User with ID " + testerId + " is not a DEVELOPER", exception.getMessage());
        verify(userService).getUserById(testerId);
        verify(projectAssignmentRepository, never()).findByUser(any(User.class));
    }

    @Test
    void getIssuesAssignedToDeveloper_ReturnsEmptyListIfDeveloperHasNoIssues_Success() {
        when(userService.getUserById(developerId)).thenReturn(developerUser);
        when(projectAssignmentRepository.findByUser(developerUser)).thenReturn(List.of());

        List<Issue> result = issueService.getIssuesAssignedToDeveloper(developerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(userService).getUserById(developerId);
        verify(projectAssignmentRepository).findByUser(developerUser);
    }

    // ========== getIssueHistory() Tests ==========

    @Test
    void getIssueHistory_ReturnsHistoryRecordsForAnIssue_Success() {
        IssueHistory history1 = new IssueHistory();
        history1.setId(UUID.randomUUID());
        history1.setIssue(testIssue);
        history1.setChangedByUser(testerUser);
        history1.setChangedAt(Instant.now());
        history1.setChangeType(ChangeType.CREATED);
        
        IssueHistory history2 = new IssueHistory();
        history2.setId(UUID.randomUUID());
        history2.setIssue(testIssue);
        history2.setChangedByUser(developerUser);
        history2.setChangedAt(Instant.now().plusSeconds(10));
        history2.setChangeType(ChangeType.STATUS_CHANGE);
        
        List<IssueHistory> histories = List.of(history2, history1);
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(issueHistoryRepository.findByIssueOrderByChangedAtDesc(testIssue)).thenReturn(histories);

        List<IssueHistory> result = issueService.getIssueHistory(issueId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(history2, result.get(0));
        assertEquals(history1, result.get(1));
        verify(issueRepository).findById(issueId);
        verify(issueHistoryRepository).findByIssueOrderByChangedAtDesc(testIssue);
    }

    @Test
    void getIssueHistory_ReturnsEmptyListIfIssueHasNoHistory_Success() {
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(issueHistoryRepository.findByIssueOrderByChangedAtDesc(testIssue)).thenReturn(List.of());

        List<IssueHistory> result = issueService.getIssueHistory(issueId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(issueRepository).findById(issueId);
        verify(issueHistoryRepository).findByIssueOrderByChangedAtDesc(testIssue);
    }

    @Test
    void getIssueHistory_HistoryRecordsAreOrderedByChangedAtDescending_Success() {
        Instant time1 = Instant.now();
        Instant time2 = time1.plusSeconds(10);
        Instant time3 = time1.plusSeconds(20);
        
        IssueHistory history1 = new IssueHistory();
        history1.setId(UUID.randomUUID());
        history1.setIssue(testIssue);
        history1.setChangedByUser(testerUser);
        history1.setChangedAt(time1);
        history1.setChangeType(ChangeType.CREATED);
        
        IssueHistory history2 = new IssueHistory();
        history2.setId(UUID.randomUUID());
        history2.setIssue(testIssue);
        history2.setChangedByUser(developerUser);
        history2.setChangedAt(time2);
        history2.setChangeType(ChangeType.STATUS_CHANGE);
        
        IssueHistory history3 = new IssueHistory();
        history3.setId(UUID.randomUUID());
        history3.setIssue(testIssue);
        history3.setChangedByUser(testerUser);
        history3.setChangedAt(time3);
        history3.setChangeType(ChangeType.FIELD_UPDATE);
        
        List<IssueHistory> histories = List.of(history3, history2, history1);
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(issueHistoryRepository.findByIssueOrderByChangedAtDesc(testIssue)).thenReturn(histories);

        List<IssueHistory> result = issueService.getIssueHistory(issueId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(history3, result.get(0));
        assertEquals(history2, result.get(1));
        assertEquals(history1, result.get(2));
        assertTrue(result.get(0).getChangedAt().isAfter(result.get(1).getChangedAt()));
        assertTrue(result.get(1).getChangedAt().isAfter(result.get(2).getChangedAt()));
        verify(issueRepository).findById(issueId);
        verify(issueHistoryRepository).findByIssueOrderByChangedAtDesc(testIssue);
    }

    @Test
    void getIssueHistory_NonExistentIssueThrowsException_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(issueRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.getIssueHistory(nonExistentId);
        });

        assertEquals("Issue with ID " + nonExistentId + " not found", exception.getMessage());
        verify(issueRepository).findById(nonExistentId);
        verify(issueHistoryRepository, never()).findByIssueOrderByChangedAtDesc(any(Issue.class));
    }
}
