package com.capstone.safeGuard.file.domain.domain;

import com.capstone.safeGuard.member.domain.domain.Child;
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
