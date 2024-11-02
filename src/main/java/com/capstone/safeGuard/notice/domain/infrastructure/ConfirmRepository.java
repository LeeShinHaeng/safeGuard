package com.capstone.safeGuard.notice.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.notice.domain.domain.Confirm;
import com.capstone.safeGuard.member.domain.domain.Helping;
import com.capstone.safeGuard.member.domain.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ConfirmRepository extends JpaRepository<Confirm, Long> {
    ArrayList<Confirm> findAllByHelpingId(Helping helping);

    ArrayList<Confirm> findAllByChild(Child child);

    ArrayList<Confirm> findAllByReceiverId(Member member);
}
