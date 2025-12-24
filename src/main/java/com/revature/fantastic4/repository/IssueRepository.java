package com.revature.fantastic4.repository;
import com.revature.fantastic4.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID>
{
    List<Issue> findByCreator(User creator);

    List<Issue> findByAssignee(User assignee);
}