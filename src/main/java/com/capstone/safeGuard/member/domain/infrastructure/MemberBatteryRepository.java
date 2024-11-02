package com.capstone.safeGuard.member.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.member.domain.domain.MemberBattery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberBatteryRepository extends JpaRepository<MemberBattery, Long> {
    Optional<MemberBattery> findByMemberId(Member member);

}
