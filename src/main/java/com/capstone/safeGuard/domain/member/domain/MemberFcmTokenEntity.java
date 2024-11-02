package com.capstone.safeGuard.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "mem_fcm_key")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberFcmTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;
}
