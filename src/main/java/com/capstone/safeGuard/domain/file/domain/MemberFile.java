package com.capstone.safeGuard.domain.file.domain;

import com.capstone.safeGuard.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class MemberFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;

    @ManyToOne
    private Member member;

    @Builder
    public MemberFile(String fileName, Member member) {
        this.fileName = fileName;
        this.member = member;
    }
}
