package com.study.FlowTrack.repository.specifications;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.model.User;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {
    public static Specification<Task> hasProject(Project project) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("project"),project));

    }

    public static Specification<Task> hasAssignedUser(User assignedUser) {
        if (assignedUser == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assignedUser"),assignedUser));
    }

    public static Specification<Task> hasCreator(User creator) {
        if (creator == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("creator"), creator);
    }
}
