package com.revature.fantastic4.repository;


import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>
{
    List<Project> findByCreatedBy(User createdBy);
}

