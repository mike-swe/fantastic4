package com.revature.fantastic4.repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID>
{
    List<Issue> findByCreator(User creator);

    List<Issue> findByAssignee(User assignee);
}