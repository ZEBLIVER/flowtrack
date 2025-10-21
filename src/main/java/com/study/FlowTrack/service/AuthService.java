package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.UserRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.model.Role;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.repository.RoleRepository;
import com.study.FlowTrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;

    private AuthService(UserRepository userRepository,
                        BCryptPasswordEncoder bCryptPasswordEncoder,
                        RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
    }

    public User registerUser(User user) {
        String userName = user.getUsername();
        if (userRepository.existsByUserName(userName)) {
            throw new DuplicateResourceException("Username is already taken!");
        }

        String rawPassword = user.getPassword();
        String encryptedPassword = bCryptPasswordEncoder.encode(rawPassword);

        user.setPassword(encryptedPassword);

        Role viewerRole = roleRepository.findByName(UserRole.ROLE_VIEWER)
                .orElseThrow(() -> new RuntimeException("Initial role ROLE_VIEWER not found. Database seeding failed."));

        user.getRoles().add(viewerRole);
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
