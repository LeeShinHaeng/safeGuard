package com.capstone.safeGuard.member.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.EmailAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailAuthCodeRepository extends JpaRepository<EmailAuthCode, String> {

}
