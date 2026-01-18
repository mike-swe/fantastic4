package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
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
class ProjectAssignmentRepositoryIntegrationTest {

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testerUser;
    private User developerUser;
    private Project project1;
    private Project project2;
    private ProjectAssignment assignment1;
    private ProjectAssignment assignment2;
    private ProjectAssignment assignment3;

    @BeforeEach
    void setUp() {
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);
        developerUser = createUser("developer", "dev@example.com", "password123", Role.DEVELOPER);

        User adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        project1 = createProject("Project 1", "Description 1", adminUser);
        project2 = createProject("Project 2", "Description 2", adminUser);

        assignment1 = createAssignment(project1, testerUser);
        assignment2 = createAssignment(project1, developerUser);
        assignment3 = createAssignment(project2, testerUser);
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

    private ProjectAssignment createAssignment(Project project, User user) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.setAssignedAt(Instant.now());
        return projectAssignmentRepository.save(assignment);
    }

    // Test 1: findByProject(Project project)

    @Test
    void findByProject_ProjectHasAssignments_ReturnsAllAssignments() {

        List<ProjectAssignment> result = projectAssignmentRepository.findByProject(project1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(assignment -> 
            assignment.getProject().getId().equals(project1.getId())));
        assertTrue(result.stream().anyMatch(assignment -> 
            assignment.getUser().getId().equals(testerUser.getId())));
        assertTrue(result.stream().anyMatch(assignment -> 
            assignment.getUser().getId().equals(developerUser.getId())));
    }

    @Test
    void findByProject_ProjectHasNoAssignments_ReturnsEmptyList() {

        User adminUser = createUser("admin2", "admin2@example.com", "password123", Role.ADMIN);
        Project emptyProject = createProject("Empty Project", "No assignments", adminUser);


        List<ProjectAssignment> result = projectAssignmentRepository.findByProject(emptyProject);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }


    // Test 2: findByUser(User user)

    @Test
    void findByUser_UserHasAssignments_ReturnsAllAssignments() {

        List<ProjectAssignment> result = projectAssignmentRepository.findByUser(testerUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(assignment -> 
            assignment.getUser().getId().equals(testerUser.getId())));
        assertTrue(result.stream().anyMatch(assignment -> 
            assignment.getProject().getId().equals(project1.getId())));
        assertTrue(result.stream().anyMatch(assignment -> 
            assignment.getProject().getId().equals(project2.getId())));
    }

    @Test
    void findByUser_UserHasNoAssignments_ReturnsEmptyList() {

        User newUser = createUser("newuser", "new@example.com", "password123", Role.TESTER);

        List<ProjectAssignment> result = projectAssignmentRepository.findByUser(newUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // Test 3: existsByProjectAndUser(Project project, User user)

    @Test
    void existsByProjectAndUser_AssignmentExists_ReturnsTrue() {
        //    Check if assignment exists for project1 and tester
        boolean result = projectAssignmentRepository.existsByProjectAndUser(project1, testerUser);

        assertTrue(result, "Assignment should exist");
    }

    @Test
    void existsByProjectAndUser_AssignmentDoesNotExist_ReturnsFalse() {
    
        User newUser = createUser("newuser", "new@example.com", "password123", Role.TESTER);
        User adminUser = createUser("admin2", "admin2@example.com", "password123", Role.ADMIN);
        Project newProject = createProject("New Project", "Description", adminUser);

        boolean result = projectAssignmentRepository.existsByProjectAndUser(newProject, newUser);

        assertFalse(result, "Assignment should not exist");
    }

    @Test
    void existsByProjectAndUser_DifferentProject_ReturnsFalse() {

        boolean result = projectAssignmentRepository.existsByProjectAndUser(project1, testerUser);
        assertTrue(result);
      
        boolean result2 = projectAssignmentRepository.existsByProjectAndUser(project2, developerUser);
        assertFalse(result2, "Developer is not assigned to project2");
    }
}
