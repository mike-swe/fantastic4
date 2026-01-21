package com.revature.fantastic4.repository;

import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository Integration Test for UserRepository
 * 
 * Tests all custom repository methods to ensure they work correctly
 * with a real database (H2 in-memory).
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User testerUser1;
    private User testerUser2;
    private User developerUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password123");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        testerUser1 = new User();
        testerUser1.setUsername("tester1");
        testerUser1.setPassword("password123");
        testerUser1.setEmail("tester1@example.com");
        testerUser1.setRole(Role.TESTER);
        testerUser1 = userRepository.save(testerUser1);

        testerUser2 = new User();
        testerUser2.setUsername("tester2");
        testerUser2.setPassword("password123");
        testerUser2.setEmail("tester2@example.com");
        testerUser2.setRole(Role.TESTER);
        testerUser2 = userRepository.save(testerUser2);

        developerUser = new User();
        developerUser.setUsername("developer");
        developerUser.setPassword("password123");
        developerUser.setEmail("developer@example.com");
        developerUser.setRole(Role.DEVELOPER);
        developerUser = userRepository.save(developerUser);
    }


    // Test 1: findByUsername(String username)

    @Test
    void findByUsername_UserExists_ReturnsUser() {

        Optional<User> result = userRepository.findByUsername("admin");

        
        assertTrue(result.isPresent(), "User should be found");
        User foundUser = result.get();
        assertEquals("admin", foundUser.getUsername(), "Username should match");
        assertEquals("admin@example.com", foundUser.getEmail(), "Email should match");
        assertEquals(Role.ADMIN, foundUser.getRole(), "Role should match");
        assertNotNull(foundUser.getId(), "ID should be generated");
    }

    @Test
    void findByUsername_UserDoesNotExist_ReturnsEmpty() {

        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertFalse(result.isPresent(), "User should not be found");
        assertTrue(result.isEmpty(), "Optional should be empty");
    }

    @Test
    void findByUsername_CaseSensitive_ReturnsEmptyForWrongCase() {
    //    Try to find with different case with capital 'A'
        Optional<User> result = userRepository.findByUsername("Admin"); 

        assertFalse(result.isPresent(), "Username search should be case-sensitive");
    }

    // Test 2: findByRole(Role role)

    @Test
    void findByRole_MultipleUsersExist_ReturnsAllUsersWithRole() {
        List<User> result = userRepository.findByRole(Role.TESTER);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should find 2 TESTER users");
        
        assertTrue(result.stream().allMatch(user -> user.getRole() == Role.TESTER),
            "All returned users should have TESTER role");
        
        assertTrue(result.stream().anyMatch(user -> user.getUsername().equals("tester1")),
            "Should contain tester1");
        assertTrue(result.stream().anyMatch(user -> user.getUsername().equals("tester2")),
            "Should contain tester2");
    }

    @Test
    void findByRole_SingleUserExists_ReturnsSingleUser() {
   
        List<User> result = userRepository.findByRole(Role.ADMIN);


        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should find 1 ADMIN user");
        assertEquals("admin", result.get(0).getUsername(), "Should be the admin user");
        assertEquals(Role.ADMIN, result.get(0).getRole(), "Should have ADMIN role");
    }

    @Test
    void findByRole_NoUsersExist_ReturnsEmptyList() {
        userRepository.deleteAll();
        
        User tester = new User();
        tester.setUsername("onlytester");
        tester.setPassword("pass");
        tester.setEmail("tester@test.com");
        tester.setRole(Role.TESTER);
        userRepository.save(tester);
        
        List<User> result = userRepository.findByRole(Role.ADMIN);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list when no users found");
        assertEquals(0, result.size(), "List size should be 0");
    }

    // Test 3: existsByUsernameAndRole(String username, Role role)

    @Test
    void existsByUsernameAndRole_UserExistsWithRole_ReturnsTrue() {

        boolean result = userRepository.existsByUsernameAndRole("admin", Role.ADMIN);

        assertTrue(result, "Should return true when user exists with that role");
    }

    @Test
    void existsByUsernameAndRole_UserExistsWithDifferentRole_ReturnsFalse() {

        boolean result = userRepository.existsByUsernameAndRole("admin", Role.TESTER);

        assertFalse(result, "Should return false when user has different role");
    }

    @Test
    void existsByUsernameAndRole_UserDoesNotExist_ReturnsFalse() {

        boolean result = userRepository.existsByUsernameAndRole("nonexistent", Role.ADMIN);

        assertFalse(result, "Should return false when user doesn't exist");
    }

    @Test
    void existsByUsernameAndRole_MultipleScenarios_ReturnsCorrectResults() {
        
        assertTrue(
            userRepository.existsByUsernameAndRole("tester1", Role.TESTER),
            "tester1 should exist with TESTER role"
        );
        
        assertFalse(
            userRepository.existsByUsernameAndRole("tester1", Role.ADMIN),
            "tester1 should not exist with ADMIN role"
        );
        
        assertTrue(
            userRepository.existsByUsernameAndRole("developer", Role.DEVELOPER),
            "developer should exist with DEVELOPER role"
        );
        
        assertFalse(
            userRepository.existsByUsernameAndRole("nonexistent", Role.TESTER),
            "nonexistent user should not exist"
        );
    }
}
