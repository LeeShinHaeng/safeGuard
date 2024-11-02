package com.capstone.safeGuard.file.domain.infrastructure;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.file.domain.domain.ChildFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildFileRepository extends JpaRepository<ChildFile, Long> {
    Optional<ChildFile> findByChild(Child foundChild);
}
