package com.capstone.safeGuard.domain.notice.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import com.capstone.safeGuard.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findAllBySenderId(Member member);

    List<Emergency> findAllByChild(Child child);
}
