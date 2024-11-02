package com.capstone.safeGuard.domain.notice.domain;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "confirm")
public class Confirm  {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long confirmId;
    private String title;
    private String content;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConfirmType confirmType;

    @ManyToOne
    private Helping helpingId;

    @ManyToOne
    private Member receiverId;

    @ManyToOne
    private Child child;

    private LocalDateTime createdAt;
}