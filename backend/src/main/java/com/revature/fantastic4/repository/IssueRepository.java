package com.revature.fantastic4.repository;


import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID>
{
    List<Issue> findByCreatedBy(User createdBy);

    //List<Issue> findByAssignedTo(User assignedTo);
    //We dont actually have an assignedto field in Issue
}