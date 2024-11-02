package com.capstone.safeGuard.member.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.MemberFcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcmTokenRepository extends JpaRepository<MemberFcmTokenEntity,Long> {


    @Query("SELECT entity.fcmToken FROM MemberFcmTokenEntity entity WHERE entity.memberId = :memberId")
    List<String> findAllTokenByMemberId(@Param("memberId") String memberId);

    List<MemberFcmTokenEntity> findAllByMemberIdAndFcmToken(String memberId, String fcmToken);
}
