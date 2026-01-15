package com.revature.fantastic4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;
    private UUID projectId;
    private UUID userId;

    @BeforeEach 
    void setUp() {
        testUser = new User(); 
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ADMIN);

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setPassword("password123");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        testerUser = new User();
        testerUser.setId(UUID.randomUUID());
        testerUser.setUsername("tester");
        testerUser.setPassword("password123");
        testerUser.setEmail("tester@example.com");
        testerUser.setRole(Role.TESTER);

        developerUser = new User();
        developerUser.setId(UUID.randomUUID());
        developerUser.setUsername("developer");
        developerUser.setPassword("password123");
        developerUser.setEmail("developer@example.com");
        developerUser.setRole(Role.DEVELOPER);

        testProject = new Project();
        projectId = UUID.randomUUID();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());

        userId = UUID.randomUUID();
    }


    @Test
    void authenticate_ValidCrendentials_ReturnUser(){
        when(userRepository.findByUsername("testuser"))
        .thenReturn(Optional.of(testUser));

        User result = userService.authenticate("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("password123", result.getPassword());
        
       verify(userRepository).findByUsername("testuser");
    }

    @Test
    void authenticate_InvalidUserName_ThrowsException(){
        when(userRepository.findByUsername("wronguser"))
        .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticate("wronguser", "password123");
        });
        
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException(){
        when(userRepository.findByUsername("testuser"))
        .thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticate("testuser", "wrongPassword");
        });        


    }

    @Test
    void authenticate_NullUsername_ThrowsException() {
    assertThrows(Exception.class, () -> {  
        userService.authenticate(null, "password123");
    });
    }   

    @Test
    void authenticate_NullPassword_ThrowsException() {
    when(userRepository.findByUsername("testuser"))
        .thenReturn(Optional.of(testUser));
    
    assertThrows(IllegalArgumentException.class, () -> {  
        userService.authenticate("testuser", null);
    });
    }

    // Test for addUserToProject() in UserService

    @Test
    void addUserToProject_AdminAssignsTester_Success() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testerUser));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(false);
        when(projectAssignmentRepository.save(any(ProjectAssignment.class))).thenAnswer(invocation -> {
            ProjectAssignment assignment = invocation.getArgument(0);
            assignment.setId(UUID.randomUUID());
            return assignment;
        });

        ProjectAssignment result = userService.addUserToProject(projectId, userId, adminUser);

        assertNotNull(result);
        assertEquals(testProject, result.getProject());
        assertEquals(testerUser, result.getUser());
        assertNotNull(result.getAssignedAt());
        verify(projectService).getProjectById(projectId);
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(projectAssignmentRepository).save(any(ProjectAssignment.class));
    }

    @Test
    void addUserToProject_AdminAssignsDeveloper_Success() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.of(developerUser));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(false);
        when(projectAssignmentRepository.save(any(ProjectAssignment.class))).thenAnswer(invocation -> {
            ProjectAssignment assignment = invocation.getArgument(0);
            assignment.setId(UUID.randomUUID());
            return assignment;
        });

        ProjectAssignment result = userService.addUserToProject(projectId, userId, adminUser);

        assertNotNull(result);
        assertEquals(testProject, result.getProject());
        assertEquals(developerUser, result.getUser());
        assertNotNull(result.getAssignedAt());
        verify(projectService).getProjectById(projectId);
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(projectAssignmentRepository).save(any(ProjectAssignment.class));
    }

    @Test
    void addUserToProject_AdminAssignsAdmin_ThrowsException() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, adminUser);
        });

        assertEquals("Admin users cannot be assigned to projects", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository, never()).save(any(ProjectAssignment.class));
    }

    @Test
    void addUserToProject_TesterAssignsUser_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, testerUser);
        });

        assertEquals("Only Admin users can add users to projects", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void addUserToProject_DeveloperAssignsUser_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, developerUser);
        });

        assertEquals("Only Admin users can add users to projects", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void addUserToProject_DuplicateAssignment_ThrowsException() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testerUser));
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, adminUser);
        });

        assertEquals("User is already assigned to this project", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(projectAssignmentRepository, never()).save(any(ProjectAssignment.class));
    }

    @Test
    void addUserToProject_NonExistentProject_ThrowsException() {
        when(projectService.getProjectById(projectId))
            .thenThrow(new IllegalArgumentException("Project with ID " + projectId + " not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, adminUser);
        });

        assertEquals("Project with ID " + projectId + " not found", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void addUserToProject_NonExistentUser_ThrowsException() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, adminUser);
        });

        assertEquals("User with ID " + userId + " not found", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository, never()).save(any(ProjectAssignment.class));
    }

    @Test
    void addUserToProject_NullAdminUser_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUserToProject(projectId, userId, null);
        });

        assertEquals("Admin user cannot be null", exception.getMessage());
        verify(projectService, never()).getProjectById(any(UUID.class));
        verify(userRepository, never()).findById(any(UUID.class));
    }


    // Test for getUserProjects() in UserService

    @Test
    void getUserProjects_UserWithProjects_ReturnsAllProjects(){
        when(userRepository.findById(userId)).thenReturn(Optional.of(testerUser));
        
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");
        
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        
        ProjectAssignment assignment1 = new ProjectAssignment();
        assignment1.setId(UUID.randomUUID());
        assignment1.setProject(project1);
        assignment1.setUser(testerUser);
        assignment1.setAssignedAt(Instant.now());
        
        ProjectAssignment assignment2 = new ProjectAssignment();
        assignment2.setId(UUID.randomUUID());
        assignment2.setProject(project2);
        assignment2.setUser(testerUser);
        assignment2.setAssignedAt(Instant.now());
        
        when(projectAssignmentRepository.findByUser(testerUser))
            .thenReturn(List.of(assignment1, assignment2));
        
        List<Project> result = userService.getUserProjects(userId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(project1, result.get(0));
        assertEquals(project2, result.get(1));
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository).findByUser(testerUser);

    }
    @Test
    void getUserProjects_UserWithNoProjects_ReturnsEmptyList() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testerUser));
        when(projectAssignmentRepository.findByUser(testerUser))
            .thenReturn(List.of());
        

        List<Project> result = userService.getUserProjects(userId);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository).findByUser(testerUser);
    }

    @Test
    void getUserProjects_NonExistentUser_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserProjects(userId);
        });
        
        assertEquals("User with ID " + userId + " not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(projectAssignmentRepository, never()).findByUser(any(User.class));
    }

    // Test for getProjectUsers() in UserService

    @Test
    void getProjectUsers_ProjectWithUsers_ReturnsAllUsers() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        
        ProjectAssignment assignment1 = new ProjectAssignment();
        assignment1.setId(UUID.randomUUID());
        assignment1.setProject(testProject);
        assignment1.setUser(testerUser);
        assignment1.setAssignedAt(Instant.now());
        
        ProjectAssignment assignment2 = new ProjectAssignment();
        assignment2.setId(UUID.randomUUID());
        assignment2.setProject(testProject);
        assignment2.setUser(developerUser);
        assignment2.setAssignedAt(Instant.now());
        
        when(projectAssignmentRepository.findByProject(testProject))
            .thenReturn(List.of(assignment1, assignment2));
        
        List<User> result = userService.getProjectUsers(projectId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testerUser, result.get(0));
        assertEquals(developerUser, result.get(1));
        verify(projectService).getProjectById(projectId);
        verify(projectAssignmentRepository).findByProject(testProject);
    }

    @Test
    void getProjectUsers_ProjectWithNoUsers_ReturnsEmptyList() {
        when(projectService.getProjectById(projectId)).thenReturn(testProject);
        when(projectAssignmentRepository.findByProject(testProject))
            .thenReturn(List.of());
        
        List<User> result = userService.getProjectUsers(projectId);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(projectService).getProjectById(projectId);
        verify(projectAssignmentRepository).findByProject(testProject);
    }

    @Test
    void getProjectUsers_NonExistentProject_ThrowsException() {
        when(projectService.getProjectById(projectId))
            .thenThrow(new IllegalArgumentException("Project with ID " + projectId + " not found"));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getProjectUsers(projectId);
        });
        
        assertEquals("Project with ID " + projectId + " not found", exception.getMessage());
        verify(projectService).getProjectById(projectId);
        verify(projectAssignmentRepository, never()).findByProject(any(Project.class));
    }

    // Test for getAllUsers() in UserService

    @Test
    void getAllUsers_UsersExist_ReturnsAllUsers() {
        when(userRepository.findAll())
            .thenReturn(List.of(adminUser, testerUser, developerUser));
        
        List<User> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(adminUser, result.get(0));
        assertEquals(testerUser, result.get(1));
        assertEquals(developerUser, result.get(2));
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_NoUsersExist_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        
        List<User> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    // Test for getUserById() in UserService

    @Test
    void getUserById_UserExists_ReturnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testerUser));
        
        User result = userService.getUserById(userId);
        
        assertNotNull(result);
        assertEquals(testerUser, result);
        assertEquals("tester", result.getUsername());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(userId);
        });
        
        assertEquals("User with ID " + userId + " not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_NullUUID_ThrowsException() {
        assertThrows(Exception.class, () -> {
            userService.getUserById(null);
        });
    }
    
}
