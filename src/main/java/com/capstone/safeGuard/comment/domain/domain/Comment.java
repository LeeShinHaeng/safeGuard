package com.capstone.safeGuard.comment.domain.domain;

import com.capstone.safeGuard.notice.domain.domain.Emergency;
import com.capstone.safeGuard.member.domain.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    private Emergency emergency;

    @ManyToOne
    private Member commentator;

    private String comment;
    private LocalDateTime createdAt;

    @Builder
    public Comment(Emergency emergency, Member commentator, String comment) {
        this.emergency = emergency;
        this.commentator = commentator;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
}
