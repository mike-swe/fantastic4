package com.revature.fantastic4.repository;
import com.revature.fantastic4.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>
{
    List<Project> findByCreator(Admin admin);
}