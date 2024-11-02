package com.capstone.safeGuard.file.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.file.domain.domain.MemberFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberFileRepository extends JpaRepository<MemberFile, Long> {
    Optional<MemberFile> findByMember(Member member);
}
