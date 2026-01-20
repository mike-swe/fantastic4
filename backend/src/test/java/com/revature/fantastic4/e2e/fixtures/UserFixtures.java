package com.revature.fantastic4.e2e.fixtures;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserFixtures {

    private static final String FIXTURES_PATH = "fixtures/users.json";
    private static List<Map<String, String>> userData;

    static {
        loadFixtures();
    }

    private static void loadFixtures() {
        try {
            ClassPathResource resource = new ClassPathResource(FIXTURES_PATH);
            InputStream inputStream = resource.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            userData = mapper.readValue(inputStream, new TypeReference<List<Map<String, String>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user fixtures from " + FIXTURES_PATH, e);
        }
    }

    public static User createUserFromFixture(String username) {
        Map<String, String> userMap = userData.stream()
                .filter(u -> u.get("username").equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User fixture not found: " + username));

        User user = new User();
        user.setUsername(userMap.get("username"));
        user.setEmail(userMap.get("email"));
        user.setPassword(userMap.get("password"));
        user.setRole(Role.valueOf(userMap.get("role")));
        return user;
    }

    public static String getPassword(String username) {
        return userData.stream()
                .filter(u -> u.get("username").equals(username))
                .findFirst()
                .map(u -> u.get("password"))
                .orElseThrow(() -> new RuntimeException("User fixture not found: " + username));
    }

    public static List<String> getAllUsernames() {
        return userData.stream()
                .map(u -> u.get("username"))
                .collect(Collectors.toList());
    }
}
