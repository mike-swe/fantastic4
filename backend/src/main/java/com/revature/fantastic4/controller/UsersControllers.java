package com.revature.fantastic4.controller;

import com.revature.fantastic4.dto.LoginRequest;
import com.revature.fantastic4.dto.TokenTransport;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.service.UserService;
import com.revature.fantastic4.util.JwtUtil;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersControllers {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<TokenTransport> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        String token = jwtUtil.generateAccessToken(
            user.getId(),
            user.getUsername(),
            user.getRole()
        );
        
        TokenTransport tokenTransport = new TokenTransport();
        tokenTransport.setToken(token);
        
        return ResponseEntity.ok(tokenTransport);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/projects")
    public ResponseEntity<List<Project>> getUserProjects(@PathVariable UUID userId) {
        List<Project> projects = userService.getUserProjects(userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects/{projectId}/users")
    public ResponseEntity<List<User>> getProjectUsers(@PathVariable UUID projectId) {
        List<User> users = userService.getProjectUsers(projectId);
        return ResponseEntity.ok(users);
    }

}

