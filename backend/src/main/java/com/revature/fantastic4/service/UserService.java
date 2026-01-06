package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final ProjectService projectService;
    private final AuditService auditService;

    public UserService(
        UserRepository userRepository,
        ProjectAssignmentRepository projectAssignmentRepository,
        ProjectService projectService,
        AuditService auditService) {
        this.userRepository = userRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.projectService = projectService;
        this.auditService = auditService;
    }

    public ProjectAssignment addUserToProject(UUID projectId, UUID userId, User adminUser) {
 
        validateAdminRole(adminUser);

        Project project = projectService.getProjectById(projectId);
        User userToAssign = getUserById(userId);

        validateUserCanBeAssigned(userToAssign);

        if (projectAssignmentRepository.existsByProjectAndUser(project, userToAssign)) {
            throw new IllegalArgumentException("User is already assigned to this project");
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(userToAssign);
        assignment.setAssignedAt(Instant.now());

        ProjectAssignment savedAssignment = projectAssignmentRepository.save(assignment);
        
        try {
            String details = String.format("User '%s' (role: %s) assigned to project '%s'", 
                userToAssign.getUsername(), userToAssign.getRole(), project.getName());
            auditService.log(adminUser.getId(), "USER_ASSIGNED_TO_PROJECT", "PROJECT_ASSIGNMENT", 
                savedAssignment.getId(), details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
        
        return savedAssignment;
    }

    private void validateAdminRole(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Admin user cannot be null");
        }
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only Admin users can add users to projects");
        }
    }

    private void validateUserCanBeAssigned(User user) {
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin users cannot be assigned to projects");
        }
        if (user.getRole() != Role.TESTER && user.getRole() != Role.DEVELOPER) {
            throw new IllegalArgumentException("Only Testers and Developers can be assigned to projects");
        }
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
    }

    public List<Project> getUserProjects(UUID userId) {
        User user = getUserById(userId);
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByUser(user);
        return assignments.stream()
                .map(ProjectAssignment::getProject)
                .collect(Collectors.toList());
    }

    public List<User> getProjectUsers(UUID projectId) {
        Project project = projectService.getProjectById(projectId);
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByProject(project);
        return assignments.stream()
                .map(ProjectAssignment::getUser)
                .collect(Collectors.toList());
    }
}

