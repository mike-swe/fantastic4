package com.revature.fantastic4.repository;


import com.revature.fantastic4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.revature.fantastic4.enums.Role;
import com.revature.fantastic4.entity.ProjectAssignment;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(Role role);
    boolean existsByUsernameAndRole(String uname, Role role);
    Set<ProjectAssignment> getAllProjectsAssignedToUser(User user);


}
