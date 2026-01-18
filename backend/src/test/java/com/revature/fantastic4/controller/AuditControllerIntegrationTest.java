package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.AuditLogRepository;
import com.revature.fantastic4.repository.CommentRepository;
import com.revature.fantastic4.repository.IssueHistoryRepository;
import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import com.revature.fantastic4.repository.UserRepository;
import com.revature.fantastic4.service.AuditService;
import com.revature.fantastic4.service.ProjectService;
import com.revature.fantastic4.util.JwtUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class AuditControllerIntegrationTest {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueHistoryRepository issueHistoryRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtUtil jwtUtil;

    private User adminUser;
    private User testerUser;
    private String adminToken;
    private String testerToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        commentRepository.deleteAll();
        issueHistoryRepository.deleteAll();
        issueRepository.deleteAll();
        projectAssignmentRepository.deleteAll();
        auditLogRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = createTestUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createTestUser("tester", "tester@example.com", "password123", Role.TESTER);

        adminToken = generateToken(adminUser);
        testerToken = generateToken(testerUser);

        projectService.createProject("Test Project", "Test Description", adminUser);

        auditService.log(adminUser.getId(), "ISSUE_CREATED", "ISSUE", UUID.randomUUID(), "Test issue created");
        auditService.log(testerUser.getId(), "ISSUE_UPDATED", "ISSUE", UUID.randomUUID(), "Test issue updated");
    }

    private User createTestUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private String generateToken(User user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
    }

    // Test: GET /audit

    @Test
    void testGetAllLogs_AdminUser_ReturnsLogs() {
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/audit")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(3))
            .body("action", hasItems("PROJECT_CREATED", "ISSUE_CREATED", "ISSUE_UPDATED"));
    }

    @Test
    void testGetAllLogs_NonAdmin_Returns403() {
        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .get("/audit")
        .then()
            .statusCode(403);
    }

    @Test
    void testGetAllLogs_NoAuth_Returns401() {
        given()
        .when()
            .get("/audit")
        .then()
            .statusCode(401);
    }


    // Test: GET /audit/entity/{entityType}
   

    @Test
    void testGetLogsByEntityType_AdminUser_ReturnsLogs() {
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/audit/entity/ISSUE")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(2))
            .body("entityType", everyItem(equalTo("ISSUE")));
    }

    @Test
    void testGetLogsByEntityType_NonAdmin_Returns403() {
        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .get("/audit/entity/ISSUE")
        .then()
            .statusCode(403);
    }

    @Test
    void testGetLogsByEntityType_NoAuth_Returns401() {
        given()
        .when()
            .get("/audit/entity/ISSUE")
        .then()
            .statusCode(401);
    }


    // Test: GET /audit/actor/{actorId}

    @Test
    void testGetLogsByActor_AdminUser_ReturnsLogs() {
        UUID actorId = adminUser.getId();

        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/audit/actor/{actorId}", actorId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(2))
            .body("actorUserId", everyItem(equalTo(actorId.toString())));
    }

    @Test
    void testGetLogsByActor_NonAdmin_Returns403() {
        UUID actorId = adminUser.getId();

        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .get("/audit/actor/{actorId}", actorId)
        .then()
            .statusCode(403);
    }

    @Test
    void testGetLogsByActor_NoAuth_Returns401() {
        UUID actorId = adminUser.getId();

        given()
        .when()
            .get("/audit/actor/{actorId}", actorId)
        .then()
            .statusCode(401);
    }

    @Test
    void testGetLogsByActor_NoLogs_ReturnsEmptyList() {
        User newUser = createTestUser("newuser", "new@example.com", "password123", Role.TESTER);
        UUID actorId = newUser.getId();

        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/audit/actor/{actorId}", actorId)
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", equalTo(0));
    }
}
