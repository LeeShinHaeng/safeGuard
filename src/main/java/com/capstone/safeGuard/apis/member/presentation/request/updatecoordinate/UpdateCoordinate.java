package com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate;

public record UpdateCoordinate(
	String type,
	String id,
	double latitude,
	double longitude,
	int battery
) {
}
