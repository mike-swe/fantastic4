package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.IssueHistory;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ChangeType;
import com.revature.fantastic4.enums.IssueFieldName;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.IssueHistoryRepository;
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
    private final IssueHistoryRepository issueHistoryRepository;
    private final AuditService auditService;
    
    public IssueService(
            IssueRepository issueRepository,
            ProjectService projectService,
            ProjectAssignmentRepository projectAssignmentRepository,
            UserService userService,
            IssueHistoryRepository issueHistoryRepository,
            AuditService auditService) {
        this.issueRepository = issueRepository;
        this.projectService = projectService;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.userService = userService;
        this.issueHistoryRepository = issueHistoryRepository;
        this.auditService = auditService;
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

    private void createHistoryRecord(Issue issue, User changedByUser, ChangeType changeType, 
                                     IssueFieldName fieldName, String oldValue, String newValue) {
        try {
            IssueHistory history = new IssueHistory();
            history.setIssue(issue);
            history.setChangedByUser(changedByUser);
            history.setChangedAt(Instant.now());
            history.setChangeType(changeType);
            history.setFieldName(fieldName);
            history.setOldValue(oldValue);
            history.setNewValue(newValue);
            issueHistoryRepository.save(history);
        } catch (Exception e) {
            System.err.println("Failed to create history record: " + e.getMessage());
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
        Instant now = Instant.now();
        issue.setCreatedAt(now);
        issue.setUpdatedAt(now);
        
        Issue savedIssue = issueRepository.save(issue);
        
        createHistoryRecord(savedIssue, tester, ChangeType.CREATED, null, null, 
                           "Issue created with status: " + IssueStatus.OPEN);
        
        try {
            String details = String.format("Issue created: title='%s', severity=%s, priority=%s, status=%s", 
                savedIssue.getTitle(), savedIssue.getSeverity(), savedIssue.getPriority(), savedIssue.getStatus());
            auditService.log(tester.getId(), "ISSUE_CREATED", "ISSUE", savedIssue.getId(), details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
        
        return savedIssue;
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
        
        IssueStatus oldStatus = issue.getStatus();
        User oldAssignedTo = issue.getAssignedTo();
        
        issue.setStatus(newStatus);
        issue.setUpdatedAt(Instant.now());
        
        if (user.getRole() == Role.DEVELOPER && 
            (newStatus == IssueStatus.IN_PROGRESS || newStatus == IssueStatus.RESOLVED)) {
            issue.setAssignedTo(user);
        }
        
        if (newStatus == IssueStatus.RESOLVED) {
            issue.setResolvedAt(Instant.now());
        }
        
        Issue savedIssue = issueRepository.save(issue);
        
        if (!oldStatus.equals(newStatus)) {
            createHistoryRecord(savedIssue, user, ChangeType.STATUS_CHANGE, IssueFieldName.STATUS,
                               oldStatus.toString(), newStatus.toString());
            
            try {
                String details = String.format("Status changed: %s -> %s", oldStatus, newStatus);
                auditService.log(user.getId(), "ISSUE_STATUS_CHANGED", "ISSUE", savedIssue.getId(), details);
            } catch (Exception e) {
                System.err.println("Failed to create audit log: " + e.getMessage());
            }
        }
        
        if (oldAssignedTo == null && issue.getAssignedTo() != null) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, null,
                               "Unassigned", issue.getAssignedTo().getUsername());
        } else if (oldAssignedTo != null && issue.getAssignedTo() != null && 
                   !oldAssignedTo.getId().equals(issue.getAssignedTo().getId())) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, null,
                               oldAssignedTo.getUsername(), issue.getAssignedTo().getUsername());
        }
        
        return savedIssue;
    }

    public Issue updateIssue(UUID issueId, String title, String description, Severity severity, Priority priority, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        Issue issue = getIssueById(issueId);
        validateUserAssignedToProject(user, issue.getProject());
        
        String oldTitle = issue.getTitle();
        String oldDescription = issue.getDescription();
        Severity oldSeverity = issue.getSeverity();
        Priority oldPriority = issue.getPriority();
        
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
        
        issue.setUpdatedAt(Instant.now());
        Issue savedIssue = issueRepository.save(issue);
        
        boolean hasChanges = false;
        StringBuilder changeDetails = new StringBuilder();
        
        if (title != null && !oldTitle.equals(title.trim())) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, IssueFieldName.TITLE,
                               oldTitle, title.trim());
            hasChanges = true;
            changeDetails.append("title: ").append(oldTitle).append(" -> ").append(title.trim()).append("; ");
        }
        
        if (description != null && !oldDescription.equals(description.trim())) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, IssueFieldName.DESCRIPTION,
                               oldDescription, description.trim());
            hasChanges = true;
            changeDetails.append("description updated; ");
        }
        
        if (severity != null && !oldSeverity.equals(severity)) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, IssueFieldName.SEVERITY,
                               oldSeverity.toString(), severity.toString());
            hasChanges = true;
            changeDetails.append("severity: ").append(oldSeverity).append(" -> ").append(severity).append("; ");
        }
        
        if (priority != null && !oldPriority.equals(priority)) {
            createHistoryRecord(savedIssue, user, ChangeType.FIELD_UPDATE, IssueFieldName.PRIORITY,
                               oldPriority.toString(), priority.toString());
            hasChanges = true;
            changeDetails.append("priority: ").append(oldPriority).append(" -> ").append(priority).append("; ");
        }
        
        if (hasChanges) {
            try {
                auditService.log(user.getId(), "ISSUE_UPDATED", "ISSUE", savedIssue.getId(), changeDetails.toString());
            } catch (Exception e) {
                System.err.println("Failed to create audit log: " + e.getMessage());
            }
        }
        
        return savedIssue;
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

    public List<Issue> getIssuesAssignedToDeveloper(UUID developerId) {
        User developer = userService.getUserById(developerId);
        if (developer.getRole() != Role.DEVELOPER) {
            throw new IllegalArgumentException("User with ID " + developerId + " is not a DEVELOPER");
        }
        
        List<ProjectAssignment> assignments = 
            projectAssignmentRepository.findByUser(developer);
        
        return assignments.stream()
            .flatMap(assignment -> issueRepository.findByProject(assignment.getProject()).stream())
            .distinct()
            .toList();
    }

    public List<IssueHistory> getIssueHistory(UUID issueId){
        Issue issue = getIssueById(issueId);
        return issueHistoryRepository.findByIssueOrderByChangedAtDesc(issue);
    }

}

