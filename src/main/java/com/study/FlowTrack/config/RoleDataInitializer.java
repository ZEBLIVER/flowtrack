package com.study.FlowTrack.config;

import com.study.FlowTrack.enums.UserRole;
import com.study.FlowTrack.model.Role;
import com.study.FlowTrack.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleDataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!roleRepository.existsByName(UserRole.ROLE_ADMIN)) {
            roleRepository.save(new Role(UserRole.ROLE_ADMIN));
        }
        if (!roleRepository.existsByName(UserRole.ROLE_DEVELOPER)) {
            roleRepository.save(new Role(UserRole.ROLE_DEVELOPER));
        }
        if (!roleRepository.existsByName(UserRole.ROLE_MANAGER)) {
            roleRepository.save(new Role(UserRole.ROLE_MANAGER));
        }
        if (!roleRepository.existsByName(UserRole.ROLE_VIEWER)) {
            roleRepository.save(new Role(UserRole.ROLE_VIEWER));
        }

    }
}
