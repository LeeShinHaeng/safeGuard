package com.capstone.safeGuard.domain.member.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="child_battery")
public class ChildBattery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childBatteryId;

    @OneToOne
    private Child childName;
    private int batteryValue;

    @Builder
    public ChildBattery(Child childName, int batteryValue) {
        this.childName = childName;
        this.batteryValue = batteryValue;
    }
}
