package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.IssueHistory;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ChangeType;
import com.revature.fantastic4.enums.IssueFieldName;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class IssueHistoryRepositoryIntegrationTest {

    @Autowired
    private IssueHistoryRepository issueHistoryRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testerUser;
    private User developerUser;
    private Project testProject;
    private Issue testIssue;
    private IssueHistory history1;
    private IssueHistory history2;
    private IssueHistory history3;

    @BeforeEach
    void setUp() {
        issueHistoryRepository.deleteAll();
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        testProject = createProject("Test Project", "Description", adminUser);

        testIssue = createIssue("Test Issue", "Issue Description", testerUser);

        // Create test history records with different timestamps
        history1 = createHistory(ChangeType.STATUS_CHANGE, IssueFieldName.STATUS, 
            "OPEN", "IN_PROGRESS", testerUser, Instant.now().minusSeconds(30));
        history2 = createHistory(ChangeType.FIELD_UPDATE, IssueFieldName.TITLE, 
            "Old Title", "New Title", developerUser, Instant.now().minusSeconds(20));
        history3 = createHistory(ChangeType.STATUS_CHANGE, IssueFieldName.STATUS, 
            "IN_PROGRESS", "RESOLVED", developerUser, Instant.now().minusSeconds(10));
    }

    private User createUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project createProject(String name, String description, User createdBy) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(createdBy);
        project.setCreatedAt(Instant.now());
        return projectRepository.save(project);
    }

    private Issue createIssue(String title, String description, User createdBy) {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setStatus(IssueStatus.OPEN);
        issue.setSeverity(Severity.HIGH);
        issue.setPriority(Priority.MEDIUM);
        issue.setProject(testProject);
        issue.setCreatedBy(createdBy);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        return issueRepository.save(issue);
    }

    private IssueHistory createHistory(ChangeType changeType, IssueFieldName fieldName, 
                                      String oldValue, String newValue, User changedBy, Instant changedAt) {
        IssueHistory history = new IssueHistory();
        history.setIssue(testIssue);
        history.setChangeType(changeType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedByUser(changedBy);
        history.setChangedAt(changedAt);
        return issueHistoryRepository.save(history);
    }

    // Test: findByIssueOrderByChangedAtDesc()

    @Test
    void findByIssueOrderByChangedAtDesc_IssueHasHistory_ReturnsOrderedHistory() { 

        List<IssueHistory> result = issueHistoryRepository.findByIssueOrderByChangedAtDesc(testIssue);

        assertNotNull(result);
        assertEquals(3, result.size());
        

        assertEquals("RESOLVED", result.get(0).getNewValue()); // history3 - newest
        assertEquals("New Title", result.get(1).getNewValue()); // history2
        assertEquals("IN_PROGRESS", result.get(2).getNewValue()); // history1 - oldest
        
        assertTrue(result.get(0).getChangedAt().isAfter(result.get(1).getChangedAt()) ||
                   result.get(0).getChangedAt().equals(result.get(1).getChangedAt()));
        assertTrue(result.get(1).getChangedAt().isAfter(result.get(2).getChangedAt()) ||
                   result.get(1).getChangedAt().equals(result.get(2).getChangedAt()));
    }

    @Test
    void findByIssueOrderByChangedAtDesc_IssueHasNoHistory_ReturnsEmptyList() {

        Issue newIssue = createIssue("New Issue", "No history", testerUser);

        List<IssueHistory> result = issueHistoryRepository.findByIssueOrderByChangedAtDesc(newIssue);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByIssueOrderByChangedAtDesc_MultipleHistory_ReturnsCorrectOrder() {

        IssueHistory oldest = createHistory(ChangeType.CREATED, null, 
            null, "Created", testerUser, Instant.now().minusSeconds(100));
        IssueHistory newest = createHistory(ChangeType.FIELD_UPDATE, IssueFieldName.DESCRIPTION, 
            "Old", "New", developerUser, Instant.now());

        List<IssueHistory> result = issueHistoryRepository.findByIssueOrderByChangedAtDesc(testIssue);

    
        assertNotNull(result);
        assertTrue(result.size() >= 5); 
        
        // Verify first record is the newest
        assertEquals("New", result.get(0).getNewValue());
        assertEquals(ChangeType.FIELD_UPDATE, result.get(0).getChangeType());
        
        // Verify last record is one of the oldest
        IssueHistory lastHistory = result.get(result.size() - 1);
        assertTrue(lastHistory.getNewValue().equals("Created") || 
                   lastHistory.getChangedAt().isBefore(result.get(0).getChangedAt()));
        
        // Verify all history records are for the same issue
        assertTrue(result.stream().allMatch(history -> 
            history.getIssue().getId().equals(testIssue.getId())));
    }
}
