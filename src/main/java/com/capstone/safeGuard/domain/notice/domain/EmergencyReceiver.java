package com.capstone.safeGuard.domain.notice.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "emergency_receiver")
public class EmergencyReceiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emergencyReceiverId;

    @ManyToOne
    private Emergency emergency;

    private String receiverId;

    @Builder
    public EmergencyReceiver(Emergency emergency, String emergencyReceiverId) {
        this.emergency = emergency;
        this.receiverId = emergencyReceiverId;
    }
}
