package ru.virra.textanalyzer.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.virra.textanalyzer.persistence.entity.Role;
import ru.virra.textanalyzer.persistence.entity.UserEntity;
import ru.virra.textanalyzer.persistence.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private DatabaseUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new DatabaseUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByUsername() {
        UserEntity user = new UserEntity();
        user.setUsername("User");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("User");

        assertAll(
                () -> assertEquals("User", result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertTrue(result.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")))

        );

        verify(userRepository).findByUsername("User");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("Unknown")
        );

        verify(userRepository).findByUsername("Unknown");
    }
}