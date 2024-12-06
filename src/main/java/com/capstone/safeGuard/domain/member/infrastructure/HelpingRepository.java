package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HelpingRepository extends JpaRepository<Helping, Long> {

	void delete(Helping helping);

	Optional<Helping> findByHelperMemberIdAndChildChildName(String memberId, String childName);

	List<Helping> findAllByHelper(Member foundMember);
}
