package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.CommentRepository;
import com.revature.fantastic4.repository.IssueHistoryRepository;
import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import com.revature.fantastic4.repository.UserRepository;
import com.revature.fantastic4.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class IssueControllerIntegrationTest {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueHistoryRepository issueHistoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private String adminToken;
    private String testerToken;
    private String developerToken;
    private Project testProject;
    private Issue testIssue;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        commentRepository.deleteAll();
        issueHistoryRepository.deleteAll();
        issueRepository.deleteAll();
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = createTestUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createTestUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createTestUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        adminToken = generateToken(adminUser);
        testerToken = generateToken(testerUser);
        developerToken = generateToken(developerUser);

        testProject = createTestProject("Test Project", "Test Description", adminUser);

        createProjectAssignment(testProject, testerUser);
        createProjectAssignment(testProject, developerUser);

        testIssue = createTestIssue("Test Issue", "Issue Description", testerUser);
    }

    private User createTestUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project createTestProject(String name, String description, User creator) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(creator);
        project.setCreatedAt(Instant.now());
        return projectRepository.save(project);
    }

    private ProjectAssignment createProjectAssignment(Project project, User user) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.setAssignedAt(Instant.now());
        return projectAssignmentRepository.save(assignment);
    }

    private Issue createTestIssue(String title, String description, User creator) {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setStatus(IssueStatus.OPEN);
        issue.setSeverity(Severity.HIGH);
        issue.setPriority(Priority.MEDIUM);
        issue.setProject(testProject);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        return issueRepository.save(issue);
    }

    private String generateToken(User user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
    }


    // Test: POST /issues

    @Test
    void testCreateIssue_TesterUser_CreatesIssue() {
        Issue newIssue = new Issue();
        newIssue.setTitle("New Issue");
        newIssue.setDescription("New Issue Description");
        newIssue.setSeverity(Severity.CRITICAL);
        newIssue.setPriority(Priority.HIGH);
        newIssue.setProject(testProject);

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(newIssue)
        .when()
            .post("/issues")
        .then()
            .statusCode(201)
            .body("title", equalTo("New Issue"))
            .body("description", equalTo("New Issue Description"))
            .body("severity", equalTo("CRITICAL"))
            .body("priority", equalTo("HIGH"))
            .body("status", equalTo("OPEN"))
            .body("id", notNullValue());
    }

    @Test
    void testCreateIssue_NoAuth_Returns401() {
        Issue newIssue = new Issue();
        newIssue.setTitle("New Issue");
        newIssue.setDescription("New Issue Description");
        newIssue.setSeverity(Severity.CRITICAL);
        newIssue.setPriority(Priority.HIGH);
        newIssue.setProject(testProject);

        given()
            .contentType(ContentType.JSON)
            .body(newIssue)
        .when()
            .post("/issues")
        .then()
            .statusCode(401);
    }


    // Test: PUT /issues/{issueId}/status

    @Test
    void testUpdateIssueStatus_DeveloperUser_UpdatesStatus() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");

        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId())
        .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"))
            .body("id", equalTo(testIssue.getId().toString()));
    }

    @Test
    void testUpdateIssueStatus_TesterUser_UpdatesStatus() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "CLOSED");

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId())
        .then()
            .statusCode(200)
            .body("status", equalTo("CLOSED"))
            .body("id", equalTo(testIssue.getId().toString()));
    }

    @Test
    void testUpdateIssueStatus_NoAuth_Returns401() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId())
        .then()
            .statusCode(401);
    }

    @Test
    void testUpdateIssueStatus_InvalidId_Returns404() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        UUID invalidId = UUID.randomUUID();

        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", invalidId)
        .then()
            .statusCode(404);
    }

    // Test: PUT /issues/{issueId}

    @Test
    void testUpdateIssue_UpdatesIssueDetails() {
        Issue updateIssue = new Issue();
        updateIssue.setTitle("Updated Title");
        updateIssue.setDescription("Updated Description");
        updateIssue.setSeverity(Severity.LOW);
        updateIssue.setPriority(Priority.LOW);

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(updateIssue)
        .when()
            .put("/issues/{issueId}", testIssue.getId())
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Title"))
            .body("description", equalTo("Updated Description"))
            .body("severity", equalTo("LOW"))
            .body("priority", equalTo("LOW"))
            .body("id", equalTo(testIssue.getId().toString()));
    }

    @Test
    void testUpdateIssue_NoAuth_Returns401() {
        Issue updateIssue = new Issue();
        updateIssue.setTitle("Updated Title");
        updateIssue.setDescription("Updated Description");
        updateIssue.setSeverity(Severity.LOW);
        updateIssue.setPriority(Priority.LOW);

        given()
            .contentType(ContentType.JSON)
            .body(updateIssue)
        .when()
            .put("/issues/{issueId}", testIssue.getId())
        .then()
            .statusCode(401);
    }

    @Test
    void testUpdateIssue_InvalidId_Returns404() {
        Issue updateIssue = new Issue();
        updateIssue.setTitle("Updated Title");
        updateIssue.setDescription("Updated Description");
        updateIssue.setSeverity(Severity.LOW);
        updateIssue.setPriority(Priority.LOW);
        UUID invalidId = UUID.randomUUID();

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(updateIssue)
        .when()
            .put("/issues/{issueId}", invalidId)
        .then()
            .statusCode(404);
    }

    // Test: GET /issues

    @Test
    void testGetAllIssues_ReturnsListOfIssues() {

        createTestIssue("Issue 2", "Description 2", testerUser);

        given()
        .when()
            .get("/issues")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(2))
            .body("title", hasItems("Test Issue", "Issue 2"));
    }

    
    // Test: GET /issues/{issueId}

    @Test
    void testGetIssueById_ValidId_ReturnsIssue() {
        UUID issueId = testIssue.getId();

        given()
        .when()
            .get("/issues/{issueId}", issueId)
        .then()
            .statusCode(200)
            .body("id", equalTo(issueId.toString()))
            .body("title", equalTo("Test Issue"))
            .body("description", equalTo("Issue Description"))
            .body("status", equalTo("OPEN"));
    }

    @Test
    void testGetIssueById_InvalidId_Returns404() {
        UUID invalidId = UUID.randomUUID();

        given()
        .when()
            .get("/issues/{issueId}", invalidId)
        .then()
            .statusCode(404);
    }

    // Test: GET /issues/project/{projectId}

    @Test
    void testGetIssuesByProject_ReturnsProjectIssues() {
        UUID projectId = testProject.getId();

        given()
        .when()
            .get("/issues/project/{projectId}", projectId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].title", equalTo("Test Issue"));
    }

    
    // Test: GET /issues/user/{userId}

    @Test
    void testGetIssuesByUser_ReturnsUserIssues() {
        UUID userId = testerUser.getId();

        given()
        .when()
            .get("/issues/user/{userId}", userId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].title", equalTo("Test Issue"));
    }


    // Test: GET /issues/assigned/{developerId}

    @Test
    void testGetAssignedIssues_DeveloperUser_ReturnsAssignedIssues() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId());

        UUID developerId = developerUser.getId();

        given()
            .header("Authorization", "Bearer " + developerToken)
        .when()
            .get("/issues/assigned/{developerId}", developerId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].id", equalTo(testIssue.getId().toString()));
    }

    @Test
    void testGetAssignedIssues_NoAuth_Returns401() {
        UUID developerId = developerUser.getId();

        given()
        .when()
            .get("/issues/assigned/{developerId}", developerId)
        .then()
            .statusCode(401);
    }

    @Test
    void testGetAssignedIssues_ViewingOtherUsersIssues_Returns403() {
    
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId());

        UUID developerId = developerUser.getId();

        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .get("/issues/assigned/{developerId}", developerId)
        .then()
            .statusCode(403);
    }

    // Test: GET /issues/{issueId}/history

    @Test
    void testGetIssueHistory_ReturnsHistory() {

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/status", testIssue.getId());

        UUID issueId = testIssue.getId();

        given()
        .when()
            .get("/issues/{issueId}/history", issueId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(1));
    }

   
    // Test: POST /issues/{issueId}/comments


    @Test
    void testCreateComment_CreatesComment() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "This is a test comment");

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/issues/{issueId}/comments", testIssue.getId())
        .then()
            .statusCode(201)
            .body("content", equalTo("This is a test comment"))
            .body("id", notNullValue());
    }

    @Test
    void testCreateComment_NoAuth_Returns401() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "This is a test comment");

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/issues/{issueId}/comments", testIssue.getId())
        .then()
            .statusCode(401);
    }

    // Test: GET /issues/{issueId}/comments
  

    @Test
    void testGetComments_ReturnsComments() {

        Comment comment = new Comment();
        comment.setContent("Test comment");
        comment.setIssue(testIssue);
        comment.setAuthor(testerUser);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        commentRepository.save(comment);

        given()
        .when()
            .get("/issues/{issueId}/comments", testIssue.getId())
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", equalTo(1))
            .body("[0].content", equalTo("Test comment"));
    }


    // Test: PUT /issues/{issueId}/comments/{commentId}


    @Test
    void testUpdateComment_UpdatesComment() {
    
        Comment comment = new Comment();
        comment.setContent("Original comment");
        comment.setIssue(testIssue);
        comment.setAuthor(testerUser);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        Comment savedComment = commentRepository.save(comment);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "Updated comment");

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/comments/{commentId}", testIssue.getId(), savedComment.getId())
        .then()
            .statusCode(200)
            .body("content", equalTo("Updated comment"))
            .body("id", equalTo(savedComment.getId().toString()));
    }

    @Test
    void testUpdateComment_NoAuth_Returns401() {
        Comment comment = new Comment();
        comment.setContent("Original comment");
        comment.setIssue(testIssue);
        comment.setAuthor(testerUser);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        Comment savedComment = commentRepository.save(comment);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "Updated comment");

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/issues/{issueId}/comments/{commentId}", testIssue.getId(), savedComment.getId())
        .then()
            .statusCode(401);
    }

    
    // Test: DELETE /issues/{issueId}/comments/{commentId}


    @Test
    void testDeleteComment_DeletesComment() {

        Comment comment = new Comment();
        comment.setContent("Comment to delete");
        comment.setIssue(testIssue);
        comment.setAuthor(testerUser);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        Comment savedComment = commentRepository.save(comment);

        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .delete("/issues/{issueId}/comments/{commentId}", testIssue.getId(), savedComment.getId())
        .then()
            .statusCode(204);

       
        assertFalse(commentRepository.findById(savedComment.getId()).isPresent());
    }

    @Test
    void testDeleteComment_NoAuth_Returns401() {
        Comment comment = new Comment();
        comment.setContent("Comment to delete");
        comment.setIssue(testIssue);
        comment.setAuthor(testerUser);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        Comment savedComment = commentRepository.save(comment);

        given()
        .when()
            .delete("/issues/{issueId}/comments/{commentId}", testIssue.getId(), savedComment.getId())
        .then()
            .statusCode(401);
    }
}
