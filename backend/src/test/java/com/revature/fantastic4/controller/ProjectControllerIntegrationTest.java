package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class ProjectControllerIntegrationTest {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueHistoryRepository issueHistoryRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private String adminToken;
    private String testerToken;
    private Project testProject;

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

        testProject = createTestProject("Test Project", "Test Description", adminUser);
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

    private String generateToken(User user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
    }

    // Test: POST /projects

    @Test
    void testCreateProject_AdminUser_CreatesProject() {
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("New Description");

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(newProject)
        .when()
            .post("/projects")
        .then()
            .statusCode(201)
            .body("name", equalTo("New Project"))
            .body("description", equalTo("New Description"))
            .body("status", equalTo("ACTIVE"))
            .body("id", notNullValue());
    }

    @Test
    void testCreateProject_NonAdmin_Returns403() {
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("New Description");

        given()
            .header("Authorization", "Bearer " + testerToken)
            .contentType(ContentType.JSON)
            .body(newProject)
        .when()
            .post("/projects")
        .then()
            .statusCode(403);
    }

    @Test
    void testCreateProject_NoAuth_Returns401() {
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("New Description");

        given()
            .contentType(ContentType.JSON)
            .body(newProject)
        .when()
            .post("/projects")
        .then()
            .statusCode(401);
    }


    // Test: PUT /projects/{projectId}

    @Test
    void testUpdateProject_AdminUser_UpdatesProject() {
        Project updateProject = new Project();
        updateProject.setName("Updated Project Name");
        updateProject.setDescription("Updated Description");
        updateProject.setStatus(ProjectStatus.ARCHIVED);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(updateProject)
        .when()
            .put("/projects/{projectId}", testProject.getId())
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Project Name"))
            .body("description", equalTo("Updated Description"))
            .body("status", equalTo("ARCHIVED"))
            .body("id", equalTo(testProject.getId().toString()));
    }

    @Test
    void testUpdateProject_NoAuth_Returns401() {
        Project updateProject = new Project();
        updateProject.setName("Updated Project Name");
        updateProject.setDescription("Updated Description");
        updateProject.setStatus(ProjectStatus.ARCHIVED);

        given()
            .contentType(ContentType.JSON)
            .body(updateProject)
        .when()
            .put("/projects/{projectId}", testProject.getId())
        .then()
            .statusCode(401);
    }


    // Test: GET /projects

    @Test
    void testGetAllProjects_ReturnsListOfProjects() {
        
        createTestProject("Project 2", "Description 2", adminUser);

        given()
        .when()
            .get("/projects")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class))
            .body("size()", greaterThanOrEqualTo(2))
            .body("name", hasItems("Test Project", "Project 2"));
    }


    // Test: GET /projects/{projectId}

    @Test
    void testGetProjectById_ValidId_ReturnsProject() {
        UUID projectId = testProject.getId();

        given()
        .when()
            .get("/projects/{projectId}", projectId)
        .then()
            .statusCode(200)
            .body("id", equalTo(projectId.toString()))
            .body("name", equalTo("Test Project"))
            .body("description", equalTo("Test Description"))
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    void testGetProjectById_InvalidId_Returns404() {
        UUID invalidId = UUID.randomUUID();

        given()
        .when()
            .get("/projects/{projectId}", invalidId)
        .then()
            .statusCode(404);
    }


    // Test: POST /projects/{projectId}/assign/{userId}

    @Test
    void testAssignUserToProject_AdminUser_AssignsUser() {
        UUID projectId = testProject.getId();
        UUID userId = testerUser.getId();

        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .post("/projects/{projectId}/assign/{userId}", projectId, userId)
        .then()
            .statusCode(201)
            .body("message", equalTo("User assigned successfully"))
            .body("assignmentId", notNullValue());

        assertTrue(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser));
    }

    @Test
    void testAssignUserToProject_NonAdmin_Returns403() {
        UUID projectId = testProject.getId();
        UUID userId = developerUser.getId();

        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .post("/projects/{projectId}/assign/{userId}", projectId, userId)
        .then()
            .statusCode(403);
    }

    @Test
    void testAssignUserToProject_NoAuth_Returns401() {
        UUID projectId = testProject.getId();
        UUID userId = developerUser.getId();

        given()
        .when()
            .post("/projects/{projectId}/assign/{userId}", projectId, userId)
        .then()
            .statusCode(401);
    }


    // Test: DELETE /projects/{projectId}

    @Test
    void testDeleteProject_AdminUser_DeletesProject() {
     
        Project projectToDelete = createTestProject("To Delete", "Will be deleted", adminUser);
        UUID projectId = projectToDelete.getId();

        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/projects/{projectId}", projectId)
        .then()
            .statusCode(200)
            .body("message", equalTo("Project deleted successfully"));


        assertFalse(projectRepository.findById(projectId).isPresent());
    }

    @Test
    void testDeleteProject_NonAdmin_Returns403() {
        UUID projectId = testProject.getId();

        given()
            .header("Authorization", "Bearer " + testerToken)
        .when()
            .delete("/projects/{projectId}", projectId)
        .then()
            .statusCode(403);
    }

    @Test
    void testDeleteProject_NoAuth_Returns401() {
        UUID projectId = testProject.getId();

        given()
        .when()
            .delete("/projects/{projectId}", projectId)
        .then()
            .statusCode(401);
    }
}
