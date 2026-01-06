package com.revature.fantastic4.repository;


import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID>
{
    List<Issue> findByCreatedBy(User createdBy);
    List<Issue> findByProject(com.revature.fantastic4.entity.Project project);
    List<Issue> findByAssignedTo(User assignedTo);
    
    @EntityGraph(attributePaths = {"comments", "comments.author"})
    Optional<Issue> findById(UUID id);

  }