package com.study.FlowTrack.repository;

import com.study.FlowTrack.model.Comment;
import com.study.FlowTrack.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    List<Comment> findAllByTask(Task task);
}
