package com.study.FlowTrack.config;

import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.model.SystemRoleEntity;
import com.study.FlowTrack.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class RoleDataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        roleRepository.saveAll(Arrays.stream(SystemRole.values())
                .filter(role -> !roleRepository.existsByName(role))
                .map(role -> new SystemRoleEntity(role))
                .collect(Collectors.toSet()));
    }

}
