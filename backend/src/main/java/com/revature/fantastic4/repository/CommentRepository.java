package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Comment;
import com.revature.fantastic4.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);
}
