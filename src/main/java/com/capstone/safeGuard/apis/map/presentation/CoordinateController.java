package com.capstone.safeGuard.apis.map.presentation;

import com.capstone.safeGuard.apis.general.presentation.response.StatusOnlyResponse;
import com.capstone.safeGuard.apis.map.application.CoordinateService;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.AddAreaRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.AreaDetailRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.DeleteAreaRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.GetChildNameRequest;
import com.capstone.safeGuard.apis.map.presentation.response.AreaDetailResponse;
import com.capstone.safeGuard.apis.map.presentation.response.AreaPersistResponse;
import com.capstone.safeGuard.apis.map.presentation.response.ReadAreaResponse;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CoordinateController {
	private final CoordinateService coordinateService;

	@PostMapping("/add-safe")
	public ResponseEntity<AreaPersistResponse> addLivingArea(@RequestBody AddAreaRequest dto) {
		Long areaId = coordinateService.addLivingArea(dto);

		return ResponseEntity
			.ok(AreaPersistResponse.of(200, areaId.toString()));
	}

	@PostMapping("/add-dangerous")
	public ResponseEntity<AreaPersistResponse> addForbiddenArea(@RequestBody AddAreaRequest dto) {
		Long areaId = coordinateService.addForbiddenArea(dto);

		return ResponseEntity
			.ok(AreaPersistResponse.of(200, areaId.toString()));
	}

	@PostMapping("/delete-area")
	public ResponseEntity<StatusOnlyResponse> deleteArea(@RequestBody DeleteAreaRequest dto) {
		coordinateService.deleteArea(dto);
		return ResponseEntity.ok(StatusOnlyResponse.of(200));
	}

	// TODO 키 값이 아이디인데 DTO로 리턴하기는 어려울 것 같음, 해결할 방법 찾기
	@PostMapping("/read-areas")
	public ResponseEntity<Map<String, ReadAreaResponse>> readAreas(@RequestBody GetChildNameRequest dto) {
		HashMap<String, ReadAreaResponse> result = new HashMap<>();

		// 1. child에 저장되어 있는 coordinate 불러오기
		ArrayList<Coordinate> coordinates = coordinateService.readAreasByChild(dto.childName());

		// 2. responseDTO로 변경
		for (Coordinate coordinate : coordinates) {
			result.put(coordinate.getCoordinateId() + "",
				ReadAreaResponse.from(coordinate)
			);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/area-detail")
	public ResponseEntity<Map<String, AreaDetailResponse>> areaDetail(@RequestBody AreaDetailRequest dto) {
		Map<String, AreaDetailResponse> result = new HashMap<>();

		Coordinate coordinate = coordinateService.findAreaById(dto.areaId());
		if (coordinate == null) {
			return ResponseEntity.status(400).build();
		}

		result.put(coordinate.getCoordinateId() + "",
			AreaDetailResponse.from(coordinate)
		);

		return ResponseEntity.ok().body(result);
	}
}
