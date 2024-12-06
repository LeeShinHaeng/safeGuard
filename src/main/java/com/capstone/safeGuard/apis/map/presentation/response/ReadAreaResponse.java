package com.capstone.safeGuard.apis.map.presentation.response;

import com.capstone.safeGuard.domain.map.domain.Coordinate;
import lombok.Builder;

@Builder
public record ReadAreaResponse(
	String isLiving,
	Double XOfPointA,
	Double YOfPointA,
	Double XOfPointB,
	Double YOfPointB,
	Double XOfPointC,
	Double YOfPointC,
	Double XOfPointD,
	Double YOfPointD
) {

	public static ReadAreaResponse from(Coordinate coordinate) {
		return ReadAreaResponse.builder()
			.isLiving(coordinate.isLivingArea() + "")
			.XOfPointA(coordinate.getXOfNorthEast())
			.YOfPointA(coordinate.getYOfNorthEast())
			.XOfPointB(coordinate.getXOfNorthWest())
			.YOfPointB(coordinate.getYOfNorthWest())
			.XOfPointC(coordinate.getXOfSouthWest())
			.YOfPointC(coordinate.getYOfSouthWest())
			.XOfPointD(coordinate.getXOfSouthEast())
			.YOfPointD(coordinate.getYOfSouthEast())
			.build();
	}
}
