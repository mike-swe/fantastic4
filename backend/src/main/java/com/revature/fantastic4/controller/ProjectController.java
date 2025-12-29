package com.revature.fantastic4.controller;

import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.service.ProjectService;
import com.revature.fantastic4.service.UserService;
import com.revature.fantastic4.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody Project project,
            @RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID adminUserId = jwtUtil.extractId(token);
        User adminUser = userService.getUserById(adminUserId);
        
        Project createdProject = projectService.createProject(
                project.getName(),
                project.getDescription(),
                adminUser
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(
            @PathVariable UUID projectId,
            @RequestBody Project project,
            @RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID adminUserId = jwtUtil.extractId(token);
        User adminUser = userService.getUserById(adminUserId);
        
        Project updatedProject = projectService.updateProject(
                projectId,
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                adminUser
        );
        
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID projectId) {
        Project project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{projectId}/assign/{userId}")
    public ResponseEntity<ProjectAssignment> assignUserToProject(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID adminUserId = jwtUtil.extractId(token);
        User adminUser = userService.getUserById(adminUserId);
        
        ProjectAssignment assignment = userService.addUserToProject(projectId, userId, adminUser);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

}

