package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class IssueRepositoryIntegrationTest {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;
    private Issue issue1;
    private Issue issue2;
    private Issue issue3;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());
        testProject = projectRepository.save(testProject);

        issue1 = createIssue("Issue 1", "Description 1", testerUser, developerUser);
        issue2 = createIssue("Issue 2", "Description 2", testerUser, null);
        issue3 = createIssue("Issue 3", "Description 3", adminUser, developerUser);
    }

    private User createUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Issue createIssue(String title, String description, User createdBy, User assignedTo) {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setStatus(IssueStatus.OPEN);
        issue.setSeverity(Severity.HIGH);
        issue.setPriority(Priority.MEDIUM);
        issue.setProject(testProject);
        issue.setCreatedBy(createdBy);
        issue.setAssignedTo(assignedTo);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        return issueRepository.save(issue);
    }

    // Test 1: findByCreatedBy(User createdBy)

    @Test
    void findByCreatedBy_UserHasIssues_ReturnsAllIssues() {

        List<Issue> result = issueRepository.findByCreatedBy(testerUser);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(issue -> issue.getCreatedBy().getId().equals(testerUser.getId())));
        assertTrue(result.stream().anyMatch(issue -> issue.getTitle().equals("Issue 1")));
        assertTrue(result.stream().anyMatch(issue -> issue.getTitle().equals("Issue 2")));
        assertFalse(result.stream().anyMatch(issue -> issue.getTitle().equals("Issue 3")));
    }

    @Test
    void findByCreatedBy_UserHasNoIssues_ReturnsEmptyList() {
  
        User newUser = createUser("newuser", "new@example.com", "password123", Role.TESTER);

        List<Issue> result = issueRepository.findByCreatedBy(newUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // Test 2: findByProject(Project project)

    @Test
    void findByProject_ProjectHasIssues_ReturnsAllIssues() {
        List<Issue> result = issueRepository.findByProject(testProject);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(issue -> issue.getProject().getId().equals(testProject.getId())));
    }

    @Test
    void findByProject_ProjectHasNoIssues_ReturnsEmptyList() {

        Project emptyProject = new Project();
        emptyProject.setName("Empty Project");
        emptyProject.setDescription("No issues");
        emptyProject.setStatus(ProjectStatus.ACTIVE);
        emptyProject.setCreatedBy(adminUser);
        emptyProject.setCreatedAt(Instant.now());
        emptyProject = projectRepository.save(emptyProject);

        List<Issue> result = issueRepository.findByProject(emptyProject);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // Test 3: findByAssignedTo(User assignedTo)

    @Test
    void findByAssignedTo_UserHasAssignedIssues_ReturnsAllIssues() {

        List<Issue> result = issueRepository.findByAssignedTo(developerUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(issue -> 
            issue.getAssignedTo() != null && 
            issue.getAssignedTo().getId().equals(developerUser.getId())));
        assertTrue(result.stream().anyMatch(issue -> issue.getTitle().equals("Issue 1")));
        assertTrue(result.stream().anyMatch(issue -> issue.getTitle().equals("Issue 3")));
    }

    @Test
    void findByAssignedTo_UserHasNoAssignedIssues_ReturnsEmptyList() {

        User newUser = createUser("newdev", "newdev@example.com", "password123", Role.DEVELOPER);

    
        List<Issue> result = issueRepository.findByAssignedTo(newUser);

   
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByAssignedTo_UnassignedIssues_NotReturned() {

        List<Issue> allIssues = issueRepository.findAll();
        long unassignedCount = allIssues.stream()
            .filter(issue -> issue.getAssignedTo() == null)
            .count();
        
        assertEquals(1, unassignedCount);
        
        List<Issue> assignedIssues = issueRepository.findByAssignedTo(developerUser);
        assertTrue(assignedIssues.stream().allMatch(issue -> issue.getAssignedTo() != null));
    }


    // Test 4: findById(UUID id) with @EntityGraph

    @Test
    void findById_IssueExists_ReturnsIssueWithComments() {
 
        Comment comment1 = new Comment();
        comment1.setContent("First comment");
        comment1.setIssue(issue1);
        comment1.setAuthor(testerUser);
        comment1.setCreatedAt(Instant.now());
        comment1.setUpdatedAt(Instant.now());
        commentRepository.saveAndFlush(comment1);

        Comment comment2 = new Comment();
        comment2.setContent("Second comment");
        comment2.setIssue(issue1);
        comment2.setAuthor(developerUser);
        comment2.setCreatedAt(Instant.now().plusSeconds(10));
        comment2.setUpdatedAt(Instant.now().plusSeconds(10));
        commentRepository.saveAndFlush(comment2);

        // Clear persistence context to ensure we fetch fresh data with EntityGraph
        entityManager.clear();

        Optional<Issue> result = issueRepository.findById(issue1.getId());

        assertTrue(result.isPresent());
        Issue foundIssue = result.get();
        assertEquals("Issue 1", foundIssue.getTitle());
        
        // Verify comments are loaded (EntityGraph should eagerly load them)
        assertNotNull(foundIssue.getComments());
        assertEquals(2, foundIssue.getComments().size());
        
        // Verify comment authors are loaded
        foundIssue.getComments().forEach(comment -> {
            assertNotNull(comment.getAuthor());
            assertNotNull(comment.getAuthor().getUsername());
        });
    }

    @Test
    void findById_IssueDoesNotExist_ReturnsEmpty() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<Issue> result = issueRepository.findById(nonExistentId);

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_IssueWithoutComments_ReturnsIssueWithEmptyComments() {

        Optional<Issue> result = issueRepository.findById(issue2.getId());

        assertTrue(result.isPresent());
        Issue foundIssue = result.get();
        assertEquals("Issue 2", foundIssue.getTitle());
        
        assertNotNull(foundIssue.getComments());
        assertTrue(foundIssue.getComments().isEmpty());
    }
}
