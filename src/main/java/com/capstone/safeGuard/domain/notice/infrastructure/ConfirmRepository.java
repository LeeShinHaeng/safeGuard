package com.capstone.safeGuard.domain.notice.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ConfirmRepository extends JpaRepository<Confirm, Long> {
    ArrayList<Confirm> findAllByHelpingId(Helping helping);

    ArrayList<Confirm> findAllByChild(Child child);

    ArrayList<Confirm> findAllByReceiverId(Member member);
}
