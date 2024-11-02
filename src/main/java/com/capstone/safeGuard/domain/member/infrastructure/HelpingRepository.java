package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpingRepository extends JpaRepository<Helping, Long> {
    
    void delete(Helping helping);

    Helping findByHelper(Member helper);

    Helping findByHelper_MemberIdAndChild_ChildName(String memberId, String childName);

    List<Helping> findAllByHelper(Member foundMember);
}
