package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    public Project createProject(String name, String description, User adminUser) {
        validateAdminRole(adminUser);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        
        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description != null ? description.trim() : null);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(adminUser);
        project.setCreatedAt(Instant.now());
        
        Project savedProject = projectRepository.save(project);
        
        try {
            String details = String.format("Project created: name='%s', status=%s", savedProject.getName(), savedProject.getStatus());
            auditService.log(adminUser.getId(), "PROJECT_CREATED", "PROJECT", savedProject.getId(), details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
        
        return savedProject;
    }

    public Project updateProject(UUID projectId, String name, String description, ProjectStatus status, User adminUser) {
        validateAdminRole(adminUser);
        
        Project project = getProjectById(projectId);
        String oldName = project.getName();
        ProjectStatus oldStatus = project.getStatus();
        String oldDescription = project.getDescription();
        
        boolean hasChanges = false;
        StringBuilder changeDetails = new StringBuilder();
        
        if (name != null) {
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Project name cannot be empty");
            }
            if (!oldName.equals(name.trim())) {
                project.setName(name.trim());
                hasChanges = true;
                changeDetails.append("name: ").append(oldName).append(" -> ").append(name.trim()).append("; ");
            }
        }
        if (description != null) {
            if (!oldDescription.equals(description)){
                project.setDescription(description.trim());
                hasChanges = true;
                changeDetails.append("description updated; ");
            } 
        }
        
        if (status != null && !oldStatus.equals(status)) {
            project.setStatus(status);
            hasChanges = true;
            changeDetails.append("status: ").append(oldStatus).append(" -> ").append(status).append("; ");
        }
        
        project.setUpdatedAt(Instant.now());
        Project savedProject = projectRepository.save(project);
        
        if (hasChanges) {
            try {
                auditService.log(adminUser.getId(), "PROJECT_UPDATED", "PROJECT", savedProject.getId(), changeDetails.toString());
            } catch (Exception e) {
                System.err.println("Failed to create audit log: " + e.getMessage());
            }
        }
        
        return savedProject;
    }

    public Project getProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found"));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getAllProjectsByUser(User user) {
        return projectRepository.findByCreatedBy(user);
    }

    public void deleteProject(UUID projectId, User adminUser) {
        validateAdminRole(adminUser);
        
        Project project = getProjectById(projectId);
        UUID projectIdToLog = project.getId();
        String projectName = project.getName();
        
        projectRepository.delete(project);
        
        try {
            String details = String.format("Project deleted: name='%s'", projectName);
            auditService.log(adminUser.getId(), "PROJECT_DELETED", "PROJECT", projectIdToLog, details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    private void validateAdminRole(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Admin user cannot be null");
        }
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only Admin users can perform this action");
        }
    }

}

