package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, UUID> {
    List<ProjectAssignment> findByProject(Project project);
    List<ProjectAssignment> findByUser(User user);
    boolean existsByProjectAndUser(Project project, User user);
}

