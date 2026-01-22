package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.IssueHistory;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.service.CommentService;
import com.revature.fantastic4.service.IssueService;
import com.revature.fantastic4.service.UserService;
import com.revature.fantastic4.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/issues")
@AllArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final UserService userService;
    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Issue> createIssue(
            @RequestBody Issue issue,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID testerUserId = jwtUtil.extractId(token);
        User tester = userService.getUserById(testerUserId);
        
        Issue createdIssue = issueService.createIssue(
                issue.getTitle(),
                issue.getDescription(),
                issue.getSeverity(),
                issue.getPriority(),
                issue.getProject().getId(),
                tester
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIssue);
    }

    @PutMapping("/{issueId}/status")
    public ResponseEntity<Issue> updateIssueStatus(
            @PathVariable UUID issueId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        String statusString = requestBody.get("status");
        if (statusString == null) {
            throw new IllegalArgumentException("Status is required in request body");
        }
        
        IssueStatus newStatus = IssueStatus.valueOf(statusString.toUpperCase());
        Issue updatedIssue = issueService.updateIssueStatus(issueId, newStatus, user);
        
        return ResponseEntity.ok(updatedIssue);
    }

    @PutMapping("/{issueId}")
    public ResponseEntity<Issue> updateIssue(
            @PathVariable UUID issueId,
            @RequestBody Issue issue,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        Issue updatedIssue = issueService.updateIssue(
                issueId,
                issue.getTitle(),
                issue.getDescription(),
                issue.getSeverity(),
                issue.getPriority(),
                user
        );
        
        return ResponseEntity.ok(updatedIssue);
    }

    @GetMapping
    public ResponseEntity<List<Issue>> getAllIssues() {
        List<Issue> issues = issueService.getAllIssues();
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<Issue> getIssueById(@PathVariable UUID issueId) {
        Issue issue = issueService.getIssueById(issueId);
        return ResponseEntity.ok(issue);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Issue>> getIssuesByProject(@PathVariable UUID projectId) {
        List<Issue> issues = issueService.getIssuesByProject(projectId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Issue>> getIssuesByUser(@PathVariable UUID userId) {
        List<Issue> issues = issueService.getIssuesByUser(userId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/assigned/{developerId}")
    public ResponseEntity<List<Issue>> getAssignedIssues(
            @PathVariable UUID developerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        if (user.getRole() != Role.ADMIN && 
            !userId.equals(developerId)) {
            throw new IllegalArgumentException("You can only view your own assigned issues");
        }
        
        List<Issue> issues = issueService.getIssuesAssignedToDeveloper(developerId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{issueId}/history")
    public ResponseEntity<List<IssueHistory>> getIssueHistory(@PathVariable UUID issueId){
        List<IssueHistory>history = issueService.getIssueHistory(issueId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{issueId}/comments")
    public ResponseEntity<Comment> createComment(
            @PathVariable UUID issueId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        String content = requestBody.get("content");
        if (content == null) {
            throw new IllegalArgumentException("Content is required in request body");
        }
        
        Comment createdComment = commentService.createComment(issueId, content, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping("/{issueId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByIssue(@PathVariable UUID issueId) {
        List<Comment> comments = commentService.getCommentsByIssue(issueId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{issueId}/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable UUID issueId,
            @PathVariable UUID commentId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        String content = requestBody.get("content");
        if (content == null) {
            throw new IllegalArgumentException("Content is required in request body");
        }
        
        Comment updatedComment = commentService.updateComment(commentId, content, user);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{issueId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID issueId,
            @PathVariable UUID commentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID userId = jwtUtil.extractId(token);
        User user = userService.getUserById(userId);
        
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }

}

