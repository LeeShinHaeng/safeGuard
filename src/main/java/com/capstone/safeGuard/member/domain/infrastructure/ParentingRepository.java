package com.capstone.safeGuard.member.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.member.domain.domain.Parenting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentingRepository extends JpaRepository<Parenting, Long> {
    List<Parenting> findAllByParent(Member member);
}
