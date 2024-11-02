package com.capstone.safeGuard.notice.domain.domain;

import com.capstone.safeGuard.comment.domain.domain.Comment;
import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.member.domain.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity @Getter @RequiredArgsConstructor @Setter
public class Emergency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long emergencyId;
    private String title;
    private String content;

    @ManyToOne
    private Member senderId;
    @ManyToOne
    private Child child;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "comment")
    @JsonIgnore
    public List<Comment> commentList;

    @Builder
    public Emergency(String title, String content, Member senderId, Child child, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.senderId = senderId;
        this.child = child;
        this.createdAt = createdAt;
    }
}