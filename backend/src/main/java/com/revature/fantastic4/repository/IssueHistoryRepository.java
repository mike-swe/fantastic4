package com.revature.fantastic4.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.revature.fantastic4.entity.Issue;
import com.revature.fantastic4.entity.IssueHistory;

@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, UUID> {
    List<IssueHistory> findByIssueOrderByChangedAtDesc(Issue issue);
} 
