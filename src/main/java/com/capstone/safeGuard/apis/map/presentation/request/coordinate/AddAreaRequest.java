package com.capstone.safeGuard.apis.map.presentation.request.coordinate;

import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.member.domain.Child;

public record AddAreaRequest(
	double xOfPointA,
	double yOfPointA,
	double xOfPointB,
	double yOfPointB,
	double xOfPointC,
	double yOfPointC,
	double xOfPointD,
	double yOfPointD,
	String childName
) {
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
