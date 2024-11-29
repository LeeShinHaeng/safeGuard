package com.capstone.safeGuard.domain.member.domain;

import static com.capstone.safeGuard.domain.member.domain.Authority.ROLE_CHILD;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
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
@Builder
@RequiredArgsConstructor
@Table(name = "child")
@AllArgsConstructor
public class Child {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String childName;

    private String childPassword;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    private double latitude;
    private double longitude;

    private String lastStatus;

    @OneToMany(mappedBy = "child", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Parenting> parentingList;

    @OneToMany(mappedBy = "child", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Helping> helpingList;

    @OneToMany(mappedBy = "child", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Coordinate> livingAreas;

    @OneToMany(mappedBy = "child", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Coordinate> forbiddenAreas;

    public static Child of(ChildRegisterRequest request, String encodedPassword) {
        return Child.builder()
            .childName(request.childName())
            .childPassword(encodedPassword)
            .authority(ROLE_CHILD)
            .latitude(0.0)
            .longitude(0.0)
            .lastStatus("일반구역")
            .parentingList(new ArrayList<>())
            .helpingList(new ArrayList<>())
            .livingAreas(new ArrayList<>())
            .forbiddenAreas(new ArrayList<>())
            .build();
    }

}
