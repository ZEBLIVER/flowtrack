package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.exception.InitialRoleNotFoundException;
import com.study.FlowTrack.model.SystemRoleEntity;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.repository.SystemRoleRepository;
import com.study.FlowTrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemRoleRepository systemRoleRepository;

    private AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        SystemRoleRepository systemRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.systemRoleRepository = systemRoleRepository;
    }

    public User registerUser(User user) {
        String userName = user.getUsername();
        if (userRepository.existsByUserName(userName)) {
            throw new DuplicateResourceException("Username is already taken!");
        }

        String rawPassword = user.getPassword();
        String encryptedPassword = passwordEncoder.encode(rawPassword);

        user.setPassword(encryptedPassword);

        SystemRoleEntity managerSystemRole = systemRoleRepository.findBySystemRoleName(SystemRole.ROLE_MANAGER)
                .orElseThrow(() -> new InitialRoleNotFoundException
                        ("Initial role ROLE_MANAGER not found. Database seeding failed."));

        user.getSystemRoles().add(managerSystemRole);
        return userRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username).
                orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username
                ));
    }
}
