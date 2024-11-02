package com.capstone.safeGuard.notice.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.notice.domain.domain.Emergency;
import com.capstone.safeGuard.member.domain.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findAllBySenderId(Member member);

    List<Emergency> findAllByChild(Child child);
}
