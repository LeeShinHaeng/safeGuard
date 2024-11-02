package com.capstone.safeGuard.member.domain.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="member_battery")
public class MemberBattery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberBatteryId;

    @OneToOne
    private Member memberId;
    private int batteryValue;

    @Builder

    public MemberBattery(Member memberId, int batteryValue) {
        this.memberId = memberId;
        this.batteryValue = batteryValue;
    }
}
