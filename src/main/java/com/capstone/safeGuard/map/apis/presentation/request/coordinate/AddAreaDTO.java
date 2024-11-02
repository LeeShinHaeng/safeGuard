package com.capstone.safeGuard.map.apis.presentation.request.coordinate;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.map.domain.domain.Coordinate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAreaDTO {
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

//    private double[] findLocation() {
//        double[] result = new double[8];
//
//        double[][] coordinates = {
//                {xOfPointA, yOfPointA},
//                {xOfPointB, yOfPointB},
//                {xOfPointC, yOfPointC},
//                {xOfPointD, yOfPointD}
//        };
//
//        double centerX = (xOfPointA + xOfPointB + xOfPointC + xOfPointD) / 4;
//        double centerY = (yOfPointA + yOfPointB + yOfPointC + yOfPointD) / 4;
//
//        for(int i = 0; i < 4; i++){
//            switch (findQuadrant(centerX, centerY, coordinates[i][0], coordinates[i][1])) {
//                case 1 -> {
//                    result[0] = coordinates[i][0];
//                    result[1] = coordinates[i][0];
//                }
//                case 2 -> {
//                    result[2] = coordinates[i][0];
//                    result[3] = coordinates[i][0];
//                }
//                case 3 -> {
//                    result[4] = coordinates[i][0];
//                    result[5] = coordinates[i][0];
//                }
//                case 4 -> {
//                    result[6] = coordinates[i][0];
//                    result[7] = coordinates[i][0];
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private int findQuadrant(double centerX, double centerY, double x, double y) {
//        if (centerX - x < 0) {
//            if (centerY - y < 0) {
//                // -- sw
//                return 3;
//            } else {
//                // -+ nw
//                return 2;
//            }
//        } else {
//            if (centerY - y < 0) {
//                return 4;
//                // +- se
//            } else {
//                return 1;
//                // ++ ne
//            }
//        }
//
//    }
}
