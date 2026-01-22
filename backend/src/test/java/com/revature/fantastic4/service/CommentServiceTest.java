package com.revature.fantastic4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.CommentRepository;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueService issueService;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CommentService commentService;

    private User adminUser;
    private User testerUser;
    private User developerUser;
    private Project testProject;
    private Issue testIssue;
    private Comment testComment;
    private UUID projectId;
    private UUID issueId;
    private UUID commentId;
    private UUID adminId;
    private UUID testerId;
    private UUID developerId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setUsername("admin");
        adminUser.setPassword("password123");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        testerId = UUID.randomUUID();
        testerUser = new User();
        testerUser.setId(testerId);
        testerUser.setUsername("tester");
        testerUser.setPassword("password123");
        testerUser.setEmail("tester@example.com");
        testerUser.setRole(Role.TESTER);

        developerId = UUID.randomUUID();
        developerUser = new User();
        developerUser.setId(developerId);
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

        issueId = UUID.randomUUID();
        testIssue = new Issue();
        testIssue.setId(issueId);
        testIssue.setTitle("Test Issue");
        testIssue.setDescription("Test Description");
        testIssue.setStatus(IssueStatus.OPEN);
        testIssue.setSeverity(Severity.MEDIUM);
        testIssue.setPriority(Priority.HIGH);
        testIssue.setProject(testProject);
        testIssue.setCreatedBy(testerUser);
        testIssue.setCreatedAt(Instant.now());
        testIssue.setUpdatedAt(Instant.now());

        commentId = UUID.randomUUID();
        testComment = new Comment();
        testComment.setId(commentId);
        testComment.setContent("Test Comment");
        testComment.setIssue(testIssue);
        testComment.setAuthor(testerUser);
        testComment.setCreatedAt(Instant.now());
        testComment.setUpdatedAt(Instant.now());
    }

    // ========== createComment() Tests ==========

    @Test
    void createComment_TesterCreatesCommentOnAssignedProject_Success() {
        String content = "This is a test comment";
        Instant beforeCreation = Instant.now();

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        Comment result = commentService.createComment(issueId, content, testerUser);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(testIssue, result.getIssue());
        assertEquals(testerUser, result.getAuthor());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)) || 
                   result.getCreatedAt().equals(beforeCreation));
        assertTrue(result.getUpdatedAt().isAfter(beforeCreation.minusSeconds(1)) || 
                   result.getUpdatedAt().equals(beforeCreation));

        verify(issueService).getIssueById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(commentRepository).save(any(Comment.class));
        verify(auditService).log(eq(testerId), eq("COMMENT_CREATED"), eq("COMMENT"), eq(result.getId()), anyString());
    }

    @Test
    void createComment_DeveloperCreatesCommentOnAssignedProject_Success() {
        String content = "Developer comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        Comment result = commentService.createComment(issueId, content, developerUser);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(testIssue, result.getIssue());
        assertEquals(developerUser, result.getAuthor());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(issueService).getIssueById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(commentRepository).save(any(Comment.class));
        verify(auditService).log(eq(developerId), eq("COMMENT_CREATED"), eq("COMMENT"), eq(result.getId()), anyString());
    }

    @Test
    void createComment_AdminCreatesComment_BypassesAssignmentCheck() {
        String content = "Admin comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        Comment result = commentService.createComment(issueId, content, adminUser);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(testIssue, result.getIssue());
        assertEquals(adminUser, result.getAuthor());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(issueService).getIssueById(issueId);
        verify(projectAssignmentRepository, never()).existsByProjectAndUser(any(Project.class), any(User.class));
        verify(commentRepository).save(any(Comment.class));
        verify(auditService).log(eq(adminId), eq("COMMENT_CREATED"), eq("COMMENT"), eq(result.getId()), anyString());
    }

    @Test
    void createComment_ContentIsTrimmed_Success() {
        String content = "  Trimmed comment  ";
        String expectedContent = "Trimmed comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        Comment result = commentService.createComment(issueId, content, testerUser);

        assertEquals(expectedContent, result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_NullAuthor_ThrowsException() {
        String content = "Test comment";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, content, null);
        });

        assertEquals("Author cannot be null", exception.getMessage());
        verify(issueService, never()).getIssueById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_NullContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, null, testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(issueService, never()).getIssueById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_EmptyContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, "", testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(issueService, never()).getIssueById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_WhitespaceOnlyContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, "   ", testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(issueService, never()).getIssueById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_TesterNotAssignedToProject_ThrowsException() {
        String content = "Test comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, content, testerUser);
        });

        assertEquals("User is not assigned to this project", exception.getMessage());
        verify(issueService).getIssueById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, testerUser);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_DeveloperNotAssignedToProject_ThrowsException() {
        String content = "Test comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, developerUser)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(issueId, content, developerUser);
        });

        assertEquals("User is not assigned to this project", exception.getMessage());
        verify(issueService).getIssueById(issueId);
        verify(projectAssignmentRepository).existsByProjectAndUser(testProject, developerUser);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_NonExistentIssue_ThrowsException() {
        String content = "Test comment";
        UUID nonExistentIssueId = UUID.randomUUID();

        when(issueService.getIssueById(nonExistentIssueId))
            .thenThrow(new IllegalArgumentException("Issue with ID " + nonExistentIssueId + " not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(nonExistentIssueId, content, testerUser);
        });

        assertEquals("Issue with ID " + nonExistentIssueId + " not found", exception.getMessage());
        verify(issueService).getIssueById(nonExistentIssueId);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void createComment_AuditLogIsCreatedWithCorrectDetails_Success() {
        String content = "Test comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        Comment result = commentService.createComment(issueId, content, testerUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(testerId), eq("COMMENT_CREATED"), eq("COMMENT"), eq(result.getId()), detailsCaptor.capture());

        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Comment created on issue:"));
        assertTrue(details.contains(testIssue.getTitle()));
    }

    @Test
    void createComment_AuditLogFailureDoesNotBreakCreation_Success() {
        String content = "Test comment";

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(projectAssignmentRepository.existsByProjectAndUser(testProject, testerUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });
        when(auditService.log(any(), anyString(), anyString(), any(), anyString()))
            .thenThrow(new RuntimeException("Audit service failure"));

        Comment result = commentService.createComment(issueId, content, testerUser);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    // ========== getCommentsByIssue() Tests ==========

    @Test
    void getCommentsByIssue_ReturnsCommentsOrderedByCreatedAtAscending_Success() {
        Comment comment1 = new Comment();
        comment1.setId(UUID.randomUUID());
        comment1.setContent("First comment");
        comment1.setIssue(testIssue);
        comment1.setAuthor(testerUser);
        comment1.setCreatedAt(Instant.now().minusSeconds(30));
        comment1.setUpdatedAt(Instant.now().minusSeconds(30));

        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setContent("Second comment");
        comment2.setIssue(testIssue);
        comment2.setAuthor(developerUser);
        comment2.setCreatedAt(Instant.now().minusSeconds(20));
        comment2.setUpdatedAt(Instant.now().minusSeconds(20));

        Comment comment3 = new Comment();
        comment3.setId(UUID.randomUUID());
        comment3.setContent("Third comment");
        comment3.setIssue(testIssue);
        comment3.setAuthor(testerUser);
        comment3.setCreatedAt(Instant.now().minusSeconds(10));
        comment3.setUpdatedAt(Instant.now().minusSeconds(10));

        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(commentRepository.findByIssueOrderByCreatedAtAsc(testIssue))
            .thenReturn(List.of(comment1, comment2, comment3));

        List<Comment> result = commentService.getCommentsByIssue(issueId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("First comment", result.get(0).getContent());
        assertEquals("Second comment", result.get(1).getContent());
        assertEquals("Third comment", result.get(2).getContent());
        assertTrue(result.get(0).getCreatedAt().isBefore(result.get(1).getCreatedAt()) ||
                   result.get(0).getCreatedAt().equals(result.get(1).getCreatedAt()));
        assertTrue(result.get(1).getCreatedAt().isBefore(result.get(2).getCreatedAt()) ||
                   result.get(1).getCreatedAt().equals(result.get(2).getCreatedAt()));

        verify(issueService).getIssueById(issueId);
        verify(commentRepository).findByIssueOrderByCreatedAtAsc(testIssue);
    }

    @Test
    void getCommentsByIssue_IssueHasNoComments_ReturnsEmptyList() {
        when(issueService.getIssueById(issueId)).thenReturn(testIssue);
        when(commentRepository.findByIssueOrderByCreatedAtAsc(testIssue)).thenReturn(List.of());

        List<Comment> result = commentService.getCommentsByIssue(issueId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(issueService).getIssueById(issueId);
        verify(commentRepository).findByIssueOrderByCreatedAtAsc(testIssue);
    }

    @Test
    void getCommentsByIssue_NonExistentIssue_ThrowsException() {
        UUID nonExistentIssueId = UUID.randomUUID();

        when(issueService.getIssueById(nonExistentIssueId))
            .thenThrow(new IllegalArgumentException("Issue with ID " + nonExistentIssueId + " not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.getCommentsByIssue(nonExistentIssueId);
        });

        assertEquals("Issue with ID " + nonExistentIssueId + " not found", exception.getMessage());
        verify(issueService).getIssueById(nonExistentIssueId);
        verify(commentRepository, never()).findByIssueOrderByCreatedAtAsc(any(Issue.class));
    }

    // ========== updateComment() Tests ==========

    @Test
    void updateComment_AuthorUpdatesOwnComment_Success() {
        String newContent = "Updated comment content";
        Instant beforeUpdate = Instant.now();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(commentId, newContent, testerUser);

        assertNotNull(result);
        assertEquals(newContent, result.getContent());
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getIssue(), result.getIssue());
        assertEquals(testComment.getAuthor(), result.getAuthor());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)) ||
                   result.getUpdatedAt().equals(beforeUpdate));

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(any(Comment.class));
        verify(auditService).log(eq(testerId), eq("COMMENT_UPDATED"), eq("COMMENT"), eq(commentId), anyString());
    }

    @Test
    void updateComment_ContentIsTrimmed_Success() {
        String newContent = "  Updated content  ";
        String expectedContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(commentId, newContent, testerUser);

        assertEquals(expectedContent, result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_UpdatedAtTimestampIsUpdated_Success() {
        String newContent = "Updated content";
        Instant originalUpdatedAt = testComment.getUpdatedAt();
        Instant beforeUpdate = Instant.now();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(commentId, newContent, testerUser);

        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(originalUpdatedAt) ||
                   result.getUpdatedAt().equals(originalUpdatedAt));
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)) ||
                   result.getUpdatedAt().equals(beforeUpdate));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_NullUser_ThrowsException() {
        String newContent = "Updated content";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, newContent, null);
        });

        assertEquals("User cannot be null", exception.getMessage());
        verify(commentRepository, never()).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_NullContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, null, testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(commentRepository, never()).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_EmptyContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, "", testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(commentRepository, never()).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_WhitespaceOnlyContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, "   ", testerUser);
        });

        assertEquals("Comment content cannot be null or empty", exception.getMessage());
        verify(commentRepository, never()).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_NonExistentComment_ThrowsException() {
        String newContent = "Updated content";
        UUID nonExistentCommentId = UUID.randomUUID();

        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(nonExistentCommentId, newContent, testerUser);
        });

        assertEquals("Comment with ID " + nonExistentCommentId + " not found", exception.getMessage());
        verify(commentRepository).findById(nonExistentCommentId);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_NonAuthorTriesToUpdate_ThrowsException() {
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, newContent, developerUser);
        });

        assertEquals("Only the comment author can update this comment", exception.getMessage());
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_AdminTriesToUpdate_ThrowsException() {
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, newContent, adminUser);
        });

        assertEquals("Only the comment author can update this comment", exception.getMessage());
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void updateComment_AuditLogIsCreatedWithCorrectDetails_Success() {
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.updateComment(commentId, newContent, testerUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(testerId), eq("COMMENT_UPDATED"), eq("COMMENT"), eq(commentId), detailsCaptor.capture());

        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Comment updated on issue:"));
        assertTrue(details.contains(testIssue.getTitle()));
    }

    // ========== deleteComment() Tests ==========

    @Test
    void deleteComment_AuthorDeletesOwnComment_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(commentId, testerUser);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(testComment);
        verify(auditService).log(eq(testerId), eq("COMMENT_DELETED"), eq("COMMENT"), eq(commentId), anyString());
    }

    @Test
    void deleteComment_AdminDeletesAnyComment_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(commentId, adminUser);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(testComment);
        verify(auditService).log(eq(adminId), eq("COMMENT_DELETED"), eq("COMMENT"), eq(commentId), anyString());
    }

    @Test
    void deleteComment_CommentIsRemovedFromRepository_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(commentId, testerUser);

        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_NullUser_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(commentId, null);
        });

        assertEquals("User cannot be null", exception.getMessage());
        verify(commentRepository, never()).findById(any(UUID.class));
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteComment_NonExistentComment_ThrowsException() {
        UUID nonExistentCommentId = UUID.randomUUID();

        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(nonExistentCommentId, testerUser);
        });

        assertEquals("Comment with ID " + nonExistentCommentId + " not found", exception.getMessage());
        verify(commentRepository).findById(nonExistentCommentId);
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteComment_NonAuthorNonAdminTriesToDelete_ThrowsException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(commentId, developerUser);
        });

        assertEquals("Only the comment author or an admin can delete this comment", exception.getMessage());
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(auditService, never()).log(any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteComment_AuditLogIsCreatedWithCorrectDetails_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(commentId, testerUser);

        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq(testerId), eq("COMMENT_DELETED"), eq("COMMENT"), eq(commentId), detailsCaptor.capture());

        String details = detailsCaptor.getValue();
        assertTrue(details.contains("Comment deleted from issue:"));
        assertTrue(details.contains(testIssue.getTitle()));
    }

    @Test
    void deleteComment_AuditLogUsesCommentDataBeforeDeletion_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(commentId, testerUser);

        verify(auditService).log(eq(testerId), eq("COMMENT_DELETED"), eq("COMMENT"), eq(commentId), anyString());
        verify(commentRepository).delete(testComment);
    }

    // ========== getCommentById() Tests ==========

    @Test
    void getCommentById_CommentExists_ReturnsComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        Comment result = commentService.getCommentById(commentId);

        assertNotNull(result);
        assertEquals(testComment, result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getContent(), result.getContent());
        assertEquals(testComment.getIssue(), result.getIssue());
        assertEquals(testComment.getAuthor(), result.getAuthor());
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_NonExistentComment_ThrowsException() {
        UUID nonExistentCommentId = UUID.randomUUID();

        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.getCommentById(nonExistentCommentId);
        });

        assertEquals("Comment with ID " + nonExistentCommentId + " not found", exception.getMessage());
        verify(commentRepository).findById(nonExistentCommentId);
    }
}
