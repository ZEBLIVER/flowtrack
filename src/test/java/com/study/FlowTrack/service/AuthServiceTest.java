package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.model.SystemRoleEntity;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.repository.SystemRoleRepository;
import com.study.FlowTrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SystemRoleRepository systemRoleRepository;

    @Test
    void registerUser_Success() {
        User newUser = new User();
        newUser.setUserName("Mike");
        newUser.setPassword("qwerty");

        SystemRoleEntity roleEntity = new SystemRoleEntity(SystemRole.ROLE_MANAGER);

        when(userRepository.existsByUserName("Mike")).thenReturn(false);
        when(passwordEncoder.encode("qwerty")).thenReturn("encryptedPassword");
        when(systemRoleRepository.findBySystemRoleName(SystemRole.ROLE_MANAGER))
                .thenReturn(Optional.of(roleEntity));
        when(userRepository.save(newUser)).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authService.registerUser(newUser);

        assertEquals("encryptedPassword",savedUser.getPassword());
        assertTrue(savedUser.getSystemRoles().contains(roleEntity));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenUserExists() {
        User newUser = new User();
        newUser.setUserName("Mike");


        when(userRepository.existsByUserName("Mike")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.registerUser(newUser));

        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void loadUserByUsername_Success() {
        User user = new User();
        user.setUserName("Mike");
        when(userRepository.findByUserName("Mike")).thenReturn(Optional.of(user));

        UserDetails result = authService.loadUserByUsername("Mike");
        assertEquals("Mike",result.getUsername());
    }

    @Test
    void loadUserByUsername_ThrowsException_WhenUsernameNotFound() {
        when(userRepository.findByUserName("Pepsi")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("Pepsi"));
    }
}