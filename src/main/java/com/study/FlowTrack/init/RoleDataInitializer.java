package com.study.FlowTrack.init;

import com.study.FlowTrack.enums.StatusTask;
import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.model.StatusTaskEntity;
import com.study.FlowTrack.model.SystemRoleEntity;
import com.study.FlowTrack.repository.StatusTaskEntityRepository;
import com.study.FlowTrack.repository.SystemRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class RoleDataInitializer implements CommandLineRunner {
    private final SystemRoleRepository systemRoleRepository;
    private final StatusTaskEntityRepository statusTaskEntityRepository;

    public RoleDataInitializer(SystemRoleRepository systemRoleRepository, StatusTaskEntityRepository statusTaskEntityRepository){
        this.systemRoleRepository = systemRoleRepository;
        this.statusTaskEntityRepository = statusTaskEntityRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        systemRoleRepository.saveAll(Arrays.stream(SystemRole.values())
                .filter(role -> !systemRoleRepository.existsBySystemRoleName(role))
                .map(role -> new SystemRoleEntity(role))
                .collect(Collectors.toSet()));

        statusTaskEntityRepository.saveAll(Arrays.stream(StatusTask.values())
                .filter(statusTask -> !statusTaskEntityRepository.existsByStatusTask(statusTask))
                .map(statusTask -> new StatusTaskEntity(statusTask))
                .collect(Collectors.toSet()));



    }

}
