package com.revature.fantastic4.controller;

import com.revature.fantastic4.dto.LoginRequest;
import com.revature.fantastic4.dto.RegisterRequest;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.CommentRepository;
import com.revature.fantastic4.repository.IssueHistoryRepository;
import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import com.revature.fantastic4.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class UsersControllerIntegrationTest {

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

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Delete in order to respect foreign key constraints (referential integrity)
        commentRepository.deleteAll();
        issueHistoryRepository.deleteAll();
        issueRepository.deleteAll();
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = createTestUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createTestUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createTestUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        testProject = createTestProject("Test Project", "Test Description", adminUser);

        createProjectAssignment(testProject, testerUser);
        createProjectAssignment(testProject, developerUser);
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

    // Test: POST /users/login

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password123");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", not(emptyString()));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLogin_NonExistentUser_Returns401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            .statusCode(401);
    }


    // Test: GET /users

    @Test
    void testGetAllUsers_ReturnsListOfUsers() {
        given()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", isA(List.class))
            .body("size()", greaterThanOrEqualTo(3))
            .body("username", hasItems("admin", "tester", "developer"));
    }


    // Test: GET /users/{userId}

    @Test
    void testGetUserById_ValidId_ReturnsUser() {
        UUID userId = adminUser.getId();

        given()
        .when()
            .get("/users/{userId}", userId)
        .then()
            .statusCode(200)
            .body("id", equalTo(userId.toString()))
            .body("username", equalTo("admin"))
            .body("email", equalTo("admin@example.com"))
            .body("role", equalTo("ADMIN"));
    }

    @Test
    void testGetUserById_InvalidId_Returns404() {
        UUID invalidId = UUID.randomUUID();
        
        when()
            .get("/users/{userId}", invalidId)
        .then()
            .statusCode(404);
    }

    // Test: GET /users/{userId}/projects

    @Test
    void testGetUserProjects_ValidUser_ReturnsProjects() {
        UUID userId = testerUser.getId();

        given()
        .when()
            .get("/users/{userId}/projects", userId)
        .then()
            .statusCode(200)
            .body("$", isA(List.class))
            .body("size()", equalTo(1))
            .body("[0].name", equalTo("Test Project"));
    }

    @Test
    void testGetUserProjects_UserWithNoProjects_ReturnsEmptyList() {
        User newUser = createTestUser("newuser", "new@example.com", "password123", Role.TESTER);

        given()
        .when()
            .get("/users/{userId}/projects", newUser.getId())
        .then()
            .statusCode(200)
            .body("$", isA(List.class))
            .body("size()", equalTo(0));
    }

 
    // Test: GET /users/projects/{projectId}/users

    @Test
    void testGetProjectUsers_ValidProject_ReturnsUsers() {
        UUID projectId = testProject.getId();

        given()
        .when()
            .get("/users/projects/{projectId}/users", projectId)
        .then()
            .statusCode(200)
            .body("$", isA(List.class))
            .body("size()", equalTo(2))
            .body("username", hasItems("tester", "developer"));
    }

    @Test
    void testGetProjectUsers_ProjectWithNoUsers_ReturnsEmptyList() {
       
        Project emptyProject = createTestProject("Empty Project", "No users", adminUser);

        given()
        .when()
            .get("/users/projects/{projectId}/users", emptyProject.getId())
        .then()
            .statusCode(200)
            .body("$", isA(List.class))
            .body("size()", equalTo(0));
    }

    // Test: POST /users/create-account

    @Test
    void testCreateAccount_ValidRequest_CreatesUser() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("TESTER");

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/users/create-account")
        .then()
            .statusCode(200)
            .body(equalTo("User Registered"));

        assertTrue(userRepository.findByUsername("newuser").isPresent());
        User createdUser = userRepository.findByUsername("newuser").get();
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertEquals(Role.TESTER, createdUser.getRole());
    }

    @Test
    void testCreateAccount_DuplicateUsername_ReturnsError() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("admin");
        registerRequest.setEmail("duplicate@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("TESTER");

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/users/create-account")
        .then()
            .statusCode(409);
    }
}
