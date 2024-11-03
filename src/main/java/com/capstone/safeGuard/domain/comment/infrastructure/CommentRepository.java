package com.capstone.safeGuard.domain.comment.infrastructure;

import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEmergency(Emergency emergencyDetail);
}
