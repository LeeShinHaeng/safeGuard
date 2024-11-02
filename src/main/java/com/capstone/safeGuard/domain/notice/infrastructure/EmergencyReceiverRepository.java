package com.capstone.safeGuard.domain.notice.infrastructure;

import com.capstone.safeGuard.domain.notice.domain.Emergency;
import com.capstone.safeGuard.domain.notice.domain.EmergencyReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyReceiverRepository extends JpaRepository<EmergencyReceiver, Long> {
    List<EmergencyReceiver> findAllByReceiverId(String memberId);

    List<EmergencyReceiver> findAllByEmergency(Emergency emergency);
}
