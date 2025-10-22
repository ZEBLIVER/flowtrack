package com.study.FlowTrack.config;

import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.model.SystemRoleEntity;
import com.study.FlowTrack.repository.SystemRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class RoleDataInitializer implements CommandLineRunner {
    private final SystemRoleRepository systemRoleRepository;

    public RoleDataInitializer(SystemRoleRepository systemRoleRepository){
        this.systemRoleRepository = systemRoleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        systemRoleRepository.saveAll(Arrays.stream(SystemRole.values())
                .filter(role -> !systemRoleRepository.existsByName(role))
                .map(role -> new SystemRoleEntity(role))
                .collect(Collectors.toSet()));
    }

}
