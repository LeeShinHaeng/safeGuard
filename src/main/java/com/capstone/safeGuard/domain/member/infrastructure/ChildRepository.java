package com.capstone.safeGuard.domain.member.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    Optional<Child> findByChildName(String name); // child_name 사용

    void delete(Child child);

    Child findBychildName(String childName);
}
