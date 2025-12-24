package com.revature.service;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import com.revature.fantastic4.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    public UserService(
        UserRepository userRepository, 
        ProjectRepository projectRepository,
        ProjectAssignmentRepository projectAssignmentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
    }

    public ProjectAssignment addUserToProject(UUID projectId, UUID userId, User adminUser) {
 
        validateAdminRole(adminUser);

        Project project = getProjectById(projectId);

        User userToAssign = getUserById(userId);

        validateUserCanBeAssigned(userToAssign);

        if (projectAssignmentRepository.existsByProjectAndUser(project, userToAssign)) {
            throw new IllegalArgumentException("User is already assigned to this project");
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(userToAssign);
        assignment.setAssignedAt(Instant.now());

        return projectAssignmentRepository.save(assignment);
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

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
    }

    private Project getProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found"));
    }
}
