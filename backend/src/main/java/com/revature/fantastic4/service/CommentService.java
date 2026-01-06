package com.revature.fantastic4.service;

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.repository.CommentRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final AuditService auditService;

    public CommentService(
            CommentRepository commentRepository,
            IssueService issueService,
            ProjectAssignmentRepository projectAssignmentRepository,
            AuditService auditService) {
        this.commentRepository = commentRepository;
        this.issueService = issueService;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.auditService = auditService;
    }

    private void validateUserAssignedToProject(User user, Project project) {
        if (user == null || project == null) {
            throw new IllegalArgumentException("User and project cannot be null");
        }
        if (user.getRole() != Role.ADMIN && !projectAssignmentRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("User is not assigned to this project");
        }
    }

    public Comment createComment(UUID issueId, String content, User author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }

        Issue issue = issueService.getIssueById(issueId);
        validateUserAssignedToProject(author, issue.getProject());

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setIssue(issue);
        comment.setAuthor(author);
        Instant now = Instant.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        Comment savedComment = commentRepository.save(comment);

        try {
            String details = String.format("Comment created on issue: '%s'", issue.getTitle());
            auditService.log(author.getId(), "COMMENT_CREATED", "COMMENT", savedComment.getId(), details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }

        return savedComment;
    }

    public List<Comment> getCommentsByIssue(UUID issueId) {
        Issue issue = issueService.getIssueById(issueId);
        return commentRepository.findByIssueOrderByCreatedAtAsc(issue);
    }

    public Comment updateComment(UUID commentId, String content, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the comment author can update this comment");
        }

        comment.setContent(content.trim());
        comment.setUpdatedAt(Instant.now());

        Comment savedComment = commentRepository.save(comment);

        try {
            String details = String.format("Comment updated on issue: '%s'", comment.getIssue().getTitle());
            auditService.log(user.getId(), "COMMENT_UPDATED", "COMMENT", savedComment.getId(), details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }

        return savedComment;
    }

    public void deleteComment(UUID commentId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found"));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("Only the comment author or an admin can delete this comment");
        }

        UUID commentIdToLog = comment.getId();
        String issueTitle = comment.getIssue().getTitle();

        commentRepository.delete(comment);

        try {
            String details = String.format("Comment deleted from issue: '%s'", issueTitle);
            auditService.log(user.getId(), "COMMENT_DELETED", "COMMENT", commentIdToLog, details);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    public Comment getCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found"));
    }
}

