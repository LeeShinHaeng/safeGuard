package com.capstone.safeGuard.notice.domain.domain;

import com.capstone.safeGuard.member.domain.domain.Child;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter

@ToString
@RequiredArgsConstructor
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long noticeId;
    private String title;
    private String content;
    private String receiverId;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private NoticeLevel noticeLevel;

    @ManyToOne
    private Child child;

    private LocalDateTime createdAt;

    public void changeTitle(String title){
        this.title=title;
    }
    public void changeContent(String content){
        this.content=content;
    }


}
