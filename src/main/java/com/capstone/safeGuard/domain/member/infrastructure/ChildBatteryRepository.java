package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.ChildBattery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildBatteryRepository extends JpaRepository<ChildBattery, Long> {
    Optional<ChildBattery> findByChildName(Child childName);

	List<ChildBattery> findByChildNameIn(List<Child> children);
}
