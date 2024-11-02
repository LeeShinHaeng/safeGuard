package com.capstone.safeGuard.domain.file.domain;

import com.capstone.safeGuard.domain.member.domain.Child;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class ChildFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;

    @ManyToOne
    private Child child;

    @Builder
    public ChildFile(String fileName, Child child) {
        this.fileName = fileName;
        this.child = child;
    }
}
