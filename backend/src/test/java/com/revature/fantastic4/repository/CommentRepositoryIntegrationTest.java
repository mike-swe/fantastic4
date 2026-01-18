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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class CommentRepositoryIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

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
    private Comment comment1;
    private Comment comment2;
    private Comment comment3;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        
        User adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createUser("developer", "dev@example.com", "password123", Role.DEVELOPER);


        testProject = createProject("Test Project", "Description", adminUser);

        testIssue = createIssue("Test Issue", "Issue Description", testerUser);

        comment1 = createComment("First comment", testerUser, Instant.now().minusSeconds(30));
        comment2 = createComment("Second comment", developerUser, Instant.now().minusSeconds(20));
        comment3 = createComment("Third comment", testerUser, Instant.now().minusSeconds(10));
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

    private Comment createComment(String content, User author, Instant createdAt) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setIssue(testIssue);
        comment.setAuthor(author);
        comment.setCreatedAt(createdAt);
        comment.setUpdatedAt(createdAt);
        return commentRepository.save(comment);
    }


    // Test: findByIssueOrderByCreatedAtAsc(Issue issue)

    @Test
    void findByIssueOrderByCreatedAtAsc_IssueHasComments_ReturnsOrderedComments() {
        List<Comment> result = commentRepository.findByIssueOrderByCreatedAtAsc(testIssue);

        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Comment1 (oldest) -> comment2 -> comment3 (newest)
        assertEquals("First comment", result.get(0).getContent());
        assertEquals("Second comment", result.get(1).getContent());
        assertEquals("Third comment", result.get(2).getContent());
        
        // Verify timestamps are in ascending order
        assertTrue(result.get(0).getCreatedAt().isBefore(result.get(1).getCreatedAt()) ||
                   result.get(0).getCreatedAt().equals(result.get(1).getCreatedAt()));
        assertTrue(result.get(1).getCreatedAt().isBefore(result.get(2).getCreatedAt()) ||
                   result.get(1).getCreatedAt().equals(result.get(2).getCreatedAt()));
    }

    @Test
    void findByIssueOrderByCreatedAtAsc_IssueHasNoComments_ReturnsEmptyList() {
        Issue newIssue = createIssue("New Issue", "No comments", testerUser);
    
        List<Comment> result = commentRepository.findByIssueOrderByCreatedAtAsc(newIssue);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByIssueOrderByCreatedAtAsc_MultipleComments_ReturnsCorrectOrder() {

        Comment oldest = createComment("Oldest", testerUser, Instant.now().minusSeconds(100));
        Comment newest = createComment("Newest", developerUser, Instant.now());

        List<Comment> result = commentRepository.findByIssueOrderByCreatedAtAsc(testIssue);

        assertNotNull(result);
        assertTrue(result.size() >= 5);

        assertEquals("Oldest", result.get(0).getContent());
        
        Comment lastComment = result.get(result.size() - 1);
        assertTrue(lastComment.getContent().equals("Newest") || 
                   lastComment.getCreatedAt().isAfter(result.get(0).getCreatedAt()));
        
        assertTrue(result.stream().allMatch(comment -> 
            comment.getIssue().getId().equals(testIssue.getId())));
    }
}
