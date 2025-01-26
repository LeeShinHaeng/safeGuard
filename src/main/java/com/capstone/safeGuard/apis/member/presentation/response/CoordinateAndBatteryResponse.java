package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record CoordinateAndBatteryResponse(
	Double latitude,
	Double longitude,
	Double battery
) {
	public static CoordinateAndBatteryResponse fromMap(Map<String, Double> map) {
		return CoordinateAndBatteryResponse.builder()
			.latitude(map.get("latitude"))
			.longitude(map.get("longitude"))
			.battery(map.get("battery"))
			.build();
	}
}
