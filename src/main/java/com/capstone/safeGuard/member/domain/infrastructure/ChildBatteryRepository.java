package com.capstone.safeGuard.member.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.member.domain.domain.ChildBattery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildBatteryRepository extends JpaRepository<ChildBattery, Long> {
    Optional<ChildBattery> findByChildName(Child childName);
}
