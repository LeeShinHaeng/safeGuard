package com.capstone.safeGuard.map.domain.domain;

import com.capstone.safeGuard.member.domain.domain.Child;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "coordinate")
public class Coordinate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long coordinateId;

    @ManyToOne
    private Child child;

    private boolean isLivingArea;

    private double xOfNorthWest;
    private double yOfNorthWest;
    private double xOfNorthEast;
    private double yOfNorthEast;
    private double xOfSouthWest;
    private double yOfSouthWest;
    private double xOfSouthEast;
    private double yOfSouthEast;


    @Builder
    public Coordinate(Child child, boolean isLivingArea,
                      double xOfNorthWest, double yOfNorthWest,
                      double xOfNorthEast, double yOfNorthEast,
                      double xOfSouthWest, double yOfSouthWest,
                      double xOfSouthEast, double yOfSouthEast) {

        this.child = child;
        this.isLivingArea = isLivingArea;
        this.xOfNorthWest = xOfNorthWest;
        this.yOfNorthWest = yOfNorthWest;
        this.xOfNorthEast = xOfNorthEast;
        this.yOfNorthEast = yOfNorthEast;
        this.xOfSouthWest = xOfSouthWest;
        this.yOfSouthWest = yOfSouthWest;
        this.xOfSouthEast = xOfSouthEast;
        this.yOfSouthEast = yOfSouthEast;
    }
}
