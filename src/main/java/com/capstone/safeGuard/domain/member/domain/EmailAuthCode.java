package com.capstone.safeGuard.domain.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @RequiredArgsConstructor @AllArgsConstructor
public class EmailAuthCode {
    @Id
    private String address;
    private String authCode;
    private LocalDateTime createdAt;
}
