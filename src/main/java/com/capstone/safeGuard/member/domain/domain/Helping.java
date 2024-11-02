package com.capstone.safeGuard.member.domain.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "helping")
public class Helping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long helpingId;

    @ManyToOne
    private Member helper;

    @ManyToOne
    private Child child;
}