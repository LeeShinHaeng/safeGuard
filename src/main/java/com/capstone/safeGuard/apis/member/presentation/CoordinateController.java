package com.capstone.safeGuard.apis.member.presentation;

import com.capstone.safeGuard.apis.map.application.CoordinateService;
import com.capstone.safeGuard.apis.member.application.BatteryService;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.CoordinateRequest;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.UpdateCoordinate;
import com.capstone.safeGuard.apis.member.presentation.response.CoordinateAndBatteryResponse;
import com.capstone.safeGuard.apis.member.presentation.response.StatusResponse;
import com.capstone.safeGuard.apis.notice.application.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CoordinateController {
	private final BatteryService batteryService;
	private final NoticeService noticeService;
	private final CoordinateService coordinateService;

	@PostMapping("/update-coordinate")
	public ResponseEntity<StatusResponse> updateCoordinate(@RequestBody UpdateCoordinate dto) {
		if (dto.type().equals("Member")) {
			coordinateService.updateMemberCoordinate(dto.id(), dto.latitude(), dto.longitude());
			batteryService.setMemberBattery(dto.id(), dto.battery());
			return ResponseEntity.ok(StatusResponse.of("200"));
		}
		coordinateService.updateChildCoordinate(dto.id(), dto.latitude(), dto.longitude());
		batteryService.setChildBattery(dto.id(), dto.battery());
		noticeService.sendNotice(dto.id());
		return ResponseEntity.ok(StatusResponse.of("200"));

	}

	@PostMapping("/return-coordinate")
	public ResponseEntity<CoordinateAndBatteryResponse> returnCoordinate(@RequestBody CoordinateRequest dto) {
		Map<String, Double> coordinates;
		if (dto.type().equals("Member")) {
			int memberBatteryValue = batteryService.getMemberBattery(dto.id());
			coordinates = coordinateService.getMemberCoordinate(dto.id());
			coordinates.put("battery", (memberBatteryValue * 1.0));

			return ResponseEntity.ok(CoordinateAndBatteryResponse.fromMap(coordinates));
		} else if (dto.type().equals("Child")) {
			int childBatteryValue = batteryService.getChildBattery(dto.id());
			coordinates = coordinateService.getChildCoordinate(dto.id());
			coordinates.put("battery", (childBatteryValue * 1.0));

			noticeService.sendNotice(dto.id());
			return ResponseEntity.ok(CoordinateAndBatteryResponse.fromMap(coordinates));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
}
