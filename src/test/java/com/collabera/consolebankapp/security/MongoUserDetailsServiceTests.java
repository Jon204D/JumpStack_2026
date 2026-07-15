package com.collabera.consolebankapp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@ExtendWith(MockitoExtension.class)
class MongoUserDetailsServiceTests {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    private MongoUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new MongoUserDetailsService(userCredentialRepository);
    }

    @Test
    void loadsAdminWithAdminAuthority() {
        UserCredential credential = new UserCredential(
                "admin", "$2a$10$encoded", Role.ADMIN, null);
        when(userCredentialRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(credential));

        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertEquals("admin", user.getUsername());
        assertEquals("$2a$10$encoded", user.getPassword());
        assertEquals("ROLE_ADMIN", user.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void rejectsUnknownUsername() {
        when(userCredentialRepository.findByUsernameIgnoreCase("missing"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing"));
    }
}
