package com.capstone.safeGuard.apis.map.presentation.request.coordinate;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAreaRequest {
    public double xOfPointA;
    public double yOfPointA;
    public double xOfPointB;
    public double yOfPointB;
    public double xOfPointC;
    public double yOfPointC;
    public double xOfPointD;
    public double yOfPointD;

    private String childName;

    public Coordinate dtoToDomain(Child child, boolean isLiving) {
        return Coordinate.builder()
                .child(child)
                .isLivingArea(isLiving)

                .xOfNorthEast(xOfPointA)
                .yOfNorthEast(yOfPointA)
                .xOfNorthWest(xOfPointB)
                .yOfNorthWest(yOfPointB)
                .xOfSouthWest(xOfPointC)
                .yOfSouthWest(yOfPointC)
                .xOfSouthEast(xOfPointD)
                .yOfSouthEast(yOfPointD)

                .build();
    }
}
