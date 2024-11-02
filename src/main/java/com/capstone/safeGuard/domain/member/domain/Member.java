package com.capstone.safeGuard.domain.member.domain;

import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "member")
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
}
