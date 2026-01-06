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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, String>> assignUserToProject(
        @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID adminUserId = jwtUtil.extractId(token);
        User adminUser = userService.getUserById(adminUserId);
        
        ProjectAssignment assignment = userService.addUserToProject(projectId, userId, adminUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User assigned successfully");
        response.put("assignmentId", assignment.getId().toString());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        UUID adminUserId = jwtUtil.extractId(token);
        User adminUser = userService.getUserById(adminUserId);
        
        projectService.deleteProject(projectId, adminUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deleted successfully");
        
        return ResponseEntity.ok(response);
    }

}

