package com.capstone.safeGuard.comment.domain.infrastructure;

import com.capstone.safeGuard.comment.domain.domain.Comment;
import com.capstone.safeGuard.notice.domain.domain.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEmergency(Emergency emergencyDetail);
}
