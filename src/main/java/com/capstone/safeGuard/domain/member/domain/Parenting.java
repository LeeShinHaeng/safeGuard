package com.capstone.safeGuard.domain.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "parenting")
public class Parenting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long parentingId;

    @ManyToOne
    private Member parent;

    @ManyToOne
    private Child child;
}
