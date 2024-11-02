package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentingRepository extends JpaRepository<Parenting, Long> {
    List<Parenting> findAllByParent(Member member);
}
