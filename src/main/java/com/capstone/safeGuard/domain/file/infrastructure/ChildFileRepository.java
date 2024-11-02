package com.capstone.safeGuard.domain.file.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.file.domain.ChildFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildFileRepository extends JpaRepository<ChildFile, Long> {
    Optional<ChildFile> findByChild(Child foundChild);
}
