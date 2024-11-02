package com.capstone.safeGuard.domain.map.infrastructure;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {

    ArrayList<Coordinate> findAllByChild(Child child);
}
