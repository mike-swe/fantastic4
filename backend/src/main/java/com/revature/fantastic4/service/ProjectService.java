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

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
        
        return projectRepository.save(project);
    }

    public Project updateProject(UUID projectId, String name, String description, ProjectStatus status, User adminUser) {
        validateAdminRole(adminUser);
        
        Project project = getProjectById(projectId);
        
        if (name != null) {
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Project name cannot be empty");
            }
            project.setName(name.trim());
        }
        
        if (description != null) {
            project.setDescription(description.trim());
        }
        
        if (status != null) {
            project.setStatus(status);
        }
        
        project.setUpdatedAt(Instant.now());
        return projectRepository.save(project);
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
        projectRepository.delete(project);
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

