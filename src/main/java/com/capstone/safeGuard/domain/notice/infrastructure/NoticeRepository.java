package com.capstone.safeGuard.domain.notice.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    ArrayList<Notice> findAllByReceiverId(String memberId);

    ArrayList<Notice> findAllByChild(Child selectedChild);
}
