package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserService userService;

    public IssueService(
            IssueRepository issueRepository,
            ProjectService projectService,
            ProjectAssignmentRepository projectAssignmentRepository,
            UserService userService) {
        this.issueRepository = issueRepository;
        this.projectService = projectService;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.userService = userService;
    }

    private void validateTesterRole(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getRole() != Role.TESTER) {
            throw new IllegalArgumentException("Only Testers can create issues");
        }
    }

    private void validateUserAssignedToProject(User user, Project project) {
        if (user == null || project == null) {
            throw new IllegalArgumentException("User and project cannot be null");
        }
        if (!projectAssignmentRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("User is not assigned to this project");
        }
    }

    private void validateStatusTransitionForRole(IssueStatus newStatus, Role userRole) {
        if (userRole == Role.DEVELOPER) {
            if (newStatus != IssueStatus.IN_PROGRESS && newStatus != IssueStatus.RESOLVED) {
                throw new IllegalArgumentException("Developers can only set status to IN_PROGRESS or RESOLVED");
            }
        } else if (userRole == Role.TESTER) {
            if (newStatus != IssueStatus.CLOSED && newStatus != IssueStatus.OPEN) {
                throw new IllegalArgumentException("Testers can only set status to CLOSED or OPEN");
            }
        } else {
            throw new IllegalArgumentException("Only Testers and Developers can update issue status");
        }
    }

    public Issue createIssue(String title, String description, Severity severity, Priority priority, UUID projectId, User tester) {
        validateTesterRole(tester);
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue title cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue description cannot be null or empty");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Issue severity cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Issue priority cannot be null");
        }
        
        Project project = projectService.getProjectById(projectId);
        validateUserAssignedToProject(tester, project);
        
        Issue issue = new Issue();
        issue.setTitle(title.trim());
        issue.setDescription(description.trim());
        issue.setSeverity(severity);
        issue.setPriority(priority);
        issue.setStatus(IssueStatus.OPEN);
        issue.setProject(project);
        issue.setCreatedBy(tester);
        issue.setCreatedAt(Instant.now());
        
        return issueRepository.save(issue);
    }

    public Issue updateIssueStatus(UUID issueId, IssueStatus newStatus, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Issue issue = getIssueById(issueId);
        validateUserAssignedToProject(user, issue.getProject());
        validateStatusTransitionForRole(newStatus, user.getRole());
        
        issue.setStatus(newStatus);
        return issueRepository.save(issue);
    }

    public Issue updateIssue(UUID issueId, String title, String description, Severity severity, Priority priority, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        Issue issue = getIssueById(issueId);
        validateUserAssignedToProject(user, issue.getProject());
        
        if (title != null) {
            if (title.trim().isEmpty()) {
                throw new IllegalArgumentException("Issue title cannot be empty");
            }
            issue.setTitle(title.trim());
        }
        
        if (description != null) {
            if (description.trim().isEmpty()) {
                throw new IllegalArgumentException("Issue description cannot be empty");
            }
            issue.setDescription(description.trim());
        }
        
        if (severity != null) {
            issue.setSeverity(severity);
        }
        
        if (priority != null) {
            issue.setPriority(priority);
        }
        
        return issueRepository.save(issue);
    }

    public Issue getIssueById(UUID issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue with ID " + issueId + " not found"));
    }

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public List<Issue> getIssuesByProject(UUID projectId) {
        Project project = projectService.getProjectById(projectId);
        return issueRepository.findByProject(project);
    }

    public List<Issue> getIssuesByUser(UUID userId) {
        User user = userService.getUserById(userId);
        return issueRepository.findByCreatedBy(user);
    }

}
