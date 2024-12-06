package com.capstone.safeGuard.domain.member.domain;

import static com.capstone.safeGuard.domain.member.domain.Authority.ROLE_MEMBER;

import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "member")
@Builder
@AllArgsConstructor
public class Member {
    @Id
    private String memberId;
    private String name;
    private String password;

    private String email;


    @Enumerated(EnumType.STRING)
    private Authority authority;

    private double latitude;
    private double longitude;

    private String fcmToken;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Parenting> parentingList;

    @OneToMany(mappedBy = "helper", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Helping> helpingList;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Comment> commented;

    public static Member of(SignUpRequest request, String encodedPassword) {
        return Member.builder()
            .memberId(request.inputID())
            .email(request.inputEmail())
            .name(request.inputName())
            .password(encodedPassword)
            .authority(ROLE_MEMBER)
            .fcmToken(request.fcmToken())
            .parentingList(new ArrayList<>())
            .helpingList(new ArrayList<>())
            .commented(new ArrayList<>())
            .build();
    }
}
