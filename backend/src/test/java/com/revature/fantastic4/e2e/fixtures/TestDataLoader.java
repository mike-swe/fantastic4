package com.revature.fantastic4.e2e.fixtures;

import com.revature.fantastic4.entity.*;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class TestDataLoader {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueHistoryRepository issueHistoryRepository;

    public User createUser(String username, String email, String password, com.revature.fantastic4.enums.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User loadOrCreateUser(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User fixture = UserFixtures.createUserFromFixture(username);
                    return createUser(fixture.getUsername(), fixture.getEmail(), fixture.getPassword(), fixture.getRole());
                });
    }

    public Project createProject(String name, String description, User createdBy) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(createdBy);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        return projectRepository.save(project);
    }

    public ProjectAssignment createProjectAssignment(Project project, User user) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.setAssignedAt(Instant.now());
        return projectAssignmentRepository.save(assignment);
    }

    public Issue createIssue(String title, String description, Project project, User createdBy, User assignedTo, 
                             Severity severity, Priority priority, IssueStatus status) {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setProject(project);
        issue.setCreatedBy(createdBy);
        issue.setAssignedTo(assignedTo);
        issue.setSeverity(severity);
        issue.setPriority(priority);
        issue.setStatus(status);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        return issueRepository.save(issue);
    }

    public void cleanupTestData() {
        commentRepository.deleteAll();
        issueHistoryRepository.deleteAll();
        issueRepository.deleteAll();
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
    // Don't delete users as they are fixtures
    }

    public void cleanupProjectAndRelatedData(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElse(null);
        if (project != null) {
            commentRepository.deleteAll();
            issueHistoryRepository.deleteAll();
            issueRepository.deleteAll();
            projectAssignmentRepository.deleteAll();
            projectRepository.delete(project);
        }
    }
}
