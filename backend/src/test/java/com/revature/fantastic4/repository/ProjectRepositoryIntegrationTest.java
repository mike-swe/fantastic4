package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Project;
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
class ProjectRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User testerUser;
    private Project project1;
    private Project project2;
    private Project project3;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = createUser("admin", "admin@example.com", "password123", Role.ADMIN);
        testerUser = createUser("tester", "tester@example.com", "password123", Role.TESTER);

        project1 = createProject("Project 1", "Description 1", adminUser);
        project2 = createProject("Project 2", "Description 2", adminUser);
        project3 = createProject("Project 3", "Description 3", testerUser);
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

    // Test: findByCreatedBy(User createdBy)

    @Test
    void findByCreatedBy_UserHasProjects_ReturnsAllProjects() {

        List<Project> result = projectRepository.findByCreatedBy(adminUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(project -> 
            project.getCreatedBy().getId().equals(adminUser.getId())));
        assertTrue(result.stream().anyMatch(project -> project.getName().equals("Project 1")));
        assertTrue(result.stream().anyMatch(project -> project.getName().equals("Project 2")));
    }

    @Test
    void findByCreatedBy_UserHasNoProjects_ReturnsEmptyList() {

        User newUser = createUser("newuser", "new@example.com", "password123", Role.ADMIN);

        List<Project> result = projectRepository.findByCreatedBy(newUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findByCreatedBy_MultipleUsers_ReturnsOnlyUserProjects() {
        List<Project> result = projectRepository.findByCreatedBy(testerUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Project 3", result.get(0).getName());
        assertEquals(testerUser.getId(), result.get(0).getCreatedBy().getId());
        
        assertFalse(result.stream().anyMatch(project -> project.getName().equals("Project 1")));
        assertFalse(result.stream().anyMatch(project -> project.getName().equals("Project 2")));
    }
}
