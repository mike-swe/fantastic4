package com.revature.fantastic4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;
    private UUID projectId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        adminUser = new User();
        adminUser.setId(adminId);
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

        projectId = UUID.randomUUID();
        testProject = new Project();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());
    }

    // ========== createProject() Tests ==========

    @Test
    void createProject_AdminCreatesProjectWithNameAndDescription_Success() {
        String projectName = "New Project";
        String projectDescription = "Project Description";
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject(projectName, projectDescription, adminUser);

        assertNotNull(result);
        assertEquals(projectName, result.getName());
        assertEquals(projectDescription, result.getDescription());
        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        assertEquals(adminUser, result.getCreatedBy());
        assertNotNull(result.getCreatedAt());
        
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_CREATED"), eq("PROJECT"), eq(result.getId()), anyString());
    }

    @Test
    void createProject_AdminCreatesProjectWithOnlyName_Success() {
        String projectName = "New Project";
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject(projectName, null, adminUser);

        assertNotNull(result);
        assertEquals(projectName, result.getName());
        assertNull(result.getDescription());
        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        assertEquals(adminUser, result.getCreatedBy());
        
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_CREATED"), eq("PROJECT"), eq(result.getId()), anyString());
    }

    @Test
    void createProject_ProjectNameCannotBeNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(null, "Description", adminUser);
        });

        assertEquals("Project name cannot be null or empty", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createProject_ProjectNameCannotBeEmpty_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("", "Description", adminUser);
        });

        assertEquals("Project name cannot be null or empty", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createProject_ProjectNameCannotBeWhitespaceOnly_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("   ", "Description", adminUser);
        });

        assertEquals("Project name cannot be null or empty", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createProject_ProjectNameIsTrimmed_Success() {
        String projectName = "  Test Project  ";
        String expectedName = "Test Project";
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject(projectName, "Description", adminUser);

        assertEquals(expectedName, result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_ProjectDescriptionIsTrimmed_Success() {
        String projectDescription = "  Test Description  ";
        String expectedDescription = "Test Description";
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject("Project Name", projectDescription, adminUser);

        assertEquals(expectedDescription, result.getDescription());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_ProjectStatusDefaultsToActive_Success() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject("Project Name", "Description", adminUser);

        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_ProjectCreatedByIsSetToAdmin_Success() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject("Project Name", "Description", adminUser);

        assertEquals(adminUser, result.getCreatedBy());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_ProjectCreatedAtIsSet_Success() {
        Instant beforeCreation = Instant.now();
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject("Project Name", "Description", adminUser);

        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)) || 
                   result.getCreatedAt().equals(beforeCreation));
        assertTrue(result.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_NonAdminCannotCreateProject_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("Project Name", "Description", testerUser);
        });

        assertEquals("Only Admin users can perform this action", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createProject_NullAdminUserThrowsException_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("Project Name", "Description", null);
        });

        assertEquals("Admin user cannot be null", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createProject_AuditLogIsCreated_Success() {
        String projectName = "New Project";
        
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project result = projectService.createProject(projectName, "Description", adminUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(adminId), eq("PROJECT_CREATED"), eq("PROJECT"), eq(result.getId()), detailsCaptor.capture());
        
        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Project created"));
        assertTrue(details.contains(projectName));
        assertTrue(details.contains("ACTIVE"));
    }

    // ========== updateProject() Tests ==========

    @Test
    void updateProject_AdminUpdatesProjectName_Success() {
        String newName = "Updated Project Name";
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, newName, null, null, adminUser);

        assertEquals(newName, result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_UPDATED"), eq("PROJECT"), eq(projectId), anyString());
    }

    @Test
    void updateProject_AdminUpdatesProjectDescription_Success() {
        String newDescription = "Updated Description";
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, null, newDescription, null, adminUser);

        assertEquals(newDescription, result.getDescription());
        assertNotNull(result.getUpdatedAt());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_UPDATED"), eq("PROJECT"), eq(projectId), anyString());
    }

    @Test
    void updateProject_AdminUpdatesProjectStatus_Success() {
        ProjectStatus newStatus = ProjectStatus.ARCHIVED;
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, null, null, newStatus, adminUser);

        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_UPDATED"), eq("PROJECT"), eq(projectId), anyString());
    }

    @Test
    void updateProject_AdminUpdatesMultipleFieldsAtOnce_Success() {
        String newName = "Updated Name";
        String newDescription = "Updated Description";
        ProjectStatus newStatus = ProjectStatus.ARCHIVED;
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, newName, newDescription, newStatus, adminUser);

        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(eq(adminId), eq("PROJECT_UPDATED"), eq("PROJECT"), eq(projectId), anyString());
    }

    @Test
    void updateProject_ProjectNameCannotBeEmptyWhenUpdating_ThrowsException() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProject(projectId, "", null, null, adminUser);
        });

        assertEquals("Project name cannot be empty", exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateProject_ProjectNameIsTrimmed_Success() {
        String newName = "  Updated Name  ";
        String expectedName = "Updated Name";
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, newName, null, null, adminUser);

        assertEquals(expectedName, result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_ProjectDescriptionIsTrimmed_Success() {
        String newDescription = "  Updated Description  ";
        String expectedDescription = "Updated Description";
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, null, newDescription, null, adminUser);

        assertEquals(expectedDescription, result.getDescription());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_ProjectUpdatedAtIsSet_Success() {
        Instant beforeUpdate = Instant.now();
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, "New Name", null, null, adminUser);

        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)) || 
                   result.getUpdatedAt().equals(beforeUpdate));
        assertTrue(result.getUpdatedAt().isBefore(Instant.now().plusSeconds(1)));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_NoChangesWhenSameValuesProvided_NoAuditLog() {
        String sameName = testProject.getName();
        String sameDescription = testProject.getDescription();
        ProjectStatus sameStatus = testProject.getStatus();
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(projectId, sameName, sameDescription, sameStatus, adminUser);

        assertNotNull(result.getUpdatedAt());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateProject_NonAdminCannotUpdateProject_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProject(projectId, "New Name", null, null, testerUser);
        });

        assertEquals("Only Admin users can perform this action", exception.getMessage());
        verify(projectRepository, never()).findById(any(UUID.class));
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateProject_NonExistentProjectThrowsException_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProject(nonExistentId, "New Name", null, null, adminUser);
        });

        assertEquals("Project with ID " + nonExistentId + " not found", exception.getMessage());
        verify(projectRepository).findById(nonExistentId);
        verify(projectRepository, never()).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateProject_AuditLogCreatedOnlyWhenChangesOccur_NoAuditLog() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        projectService.updateProject(projectId, null, null, null, adminUser);

        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    // ========== getProjectById() Tests ==========

    @Test
    void getProjectById_ReturnsProjectWhenFound_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        Project result = projectService.getProjectById(projectId);

        assertNotNull(result);
        assertEquals(testProject, result);
        assertEquals(testProject.getId(), result.getId());
        assertEquals(testProject.getName(), result.getName());
        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectById_ThrowsExceptionWhenProjectNotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.getProjectById(nonExistentId);
        });

        assertEquals("Project with ID " + nonExistentId + " not found", exception.getMessage());
        verify(projectRepository).findById(nonExistentId);
    }

    // ========== getAllProjects() Tests ==========

    @Test
    void getAllProjects_ReturnsAllProjects_Success() {
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");
        
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        
        Project project3 = new Project();
        project3.setId(UUID.randomUUID());
        project3.setName("Project 3");
        
        List<Project> projects = List.of(project1, project2, project3);
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(project1, result.get(0));
        assertEquals(project2, result.get(1));
        assertEquals(project3, result.get(2));
        verify(projectRepository).findAll();
    }

    @Test
    void getAllProjects_ReturnsEmptyListIfNoProjectsExist_Success() {
        when(projectRepository.findAll()).thenReturn(List.of());

        List<Project> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(projectRepository).findAll();
    }

    // ========== getAllProjectsByUser() Tests ==========

    @Test
    void getAllProjectsByUser_ReturnsProjectsCreatedBySpecificUser_Success() {
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");
        project1.setCreatedBy(adminUser);
        
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        project2.setCreatedBy(adminUser);
        
        List<Project> projects = List.of(project1, project2);
        when(projectRepository.findByCreatedBy(adminUser)).thenReturn(projects);

        List<Project> result = projectService.getAllProjectsByUser(adminUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(project1, result.get(0));
        assertEquals(project2, result.get(1));
        verify(projectRepository).findByCreatedBy(adminUser);
    }

    @Test
    void getAllProjectsByUser_ReturnsEmptyListIfUserCreatedNoProjects_Success() {
        when(projectRepository.findByCreatedBy(adminUser)).thenReturn(List.of());

        List<Project> result = projectService.getAllProjectsByUser(adminUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(projectRepository).findByCreatedBy(adminUser);
    }

    // ========== deleteProject() Tests ==========

    @Test
    void deleteProject_AdminCanDeleteProject_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        projectService.deleteProject(projectId, adminUser);

        verify(projectRepository).findById(projectId);
        verify(projectRepository).delete(testProject);
        verify(auditService).log(eq(adminId), eq("PROJECT_DELETED"), eq("PROJECT"), eq(projectId), anyString());
    }

    @Test
    void deleteProject_NonAdminCannotDeleteProject_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.deleteProject(projectId, testerUser);
        });

        assertEquals("Only Admin users can perform this action", exception.getMessage());
        verify(projectRepository, never()).findById(any(UUID.class));
        verify(projectRepository, never()).delete(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteProject_NonExistentProjectThrowsException_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.deleteProject(nonExistentId, adminUser);
        });

        assertEquals("Project with ID " + nonExistentId + " not found", exception.getMessage());
        verify(projectRepository).findById(nonExistentId);
        verify(projectRepository, never()).delete(any(Project.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteProject_AuditLogIsCreated_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        projectService.deleteProject(projectId, adminUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(adminId), eq("PROJECT_DELETED"), eq("PROJECT"), eq(projectId), detailsCaptor.capture());
        
        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Project deleted"));
        assertTrue(details.contains(testProject.getName()));
    }
}
