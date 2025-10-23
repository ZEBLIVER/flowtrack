package com.study.FlowTrack.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id",nullable = false,unique = true)
    private Long id;

    private String commentText;

    @ManyToOne
    @JoinColumn(name = "task",nullable = false)
    private Task task;
}
