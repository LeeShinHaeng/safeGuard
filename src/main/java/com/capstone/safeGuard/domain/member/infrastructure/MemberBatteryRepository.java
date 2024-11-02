package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.MemberBattery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberBatteryRepository extends JpaRepository<MemberBattery, Long> {
    Optional<MemberBattery> findByMemberId(Member member);

}
