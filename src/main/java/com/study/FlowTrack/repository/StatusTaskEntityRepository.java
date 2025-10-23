package com.study.FlowTrack.repository;

import com.study.FlowTrack.enums.StatusTask;
import com.study.FlowTrack.model.StatusTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusTaskEntityRepository extends JpaRepository<StatusTaskEntity,Long> {
    boolean existsByStatusTask(StatusTask name);
}
