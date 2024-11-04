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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CoordinateController {
	private final CoordinateService coordinateService;

	@PostMapping("/add-safe")
	public ResponseEntity<AreaPersistResponse> addLivingArea(@RequestBody AddAreaRequest dto) {
		Long areaId = coordinateService.addLivingArea(dto);
		if (areaId == 0L) {
			return ResponseEntity.
				status(400)
				.body(AreaPersistResponse.of(400, ""));
		}

		return ResponseEntity
			.ok(AreaPersistResponse.of(200, areaId.toString()));
	}

	@PostMapping("/add-dangerous")
	public ResponseEntity<AreaPersistResponse> addForbiddenArea(@RequestBody AddAreaRequest dto) {
		Long areaId = coordinateService.addForbiddenArea(dto);
		if (areaId == 0L) {
			return ResponseEntity.
				status(400)
				.body(AreaPersistResponse.of(400, ""));
		}

		return ResponseEntity
			.ok(AreaPersistResponse.of(200, areaId.toString()));
	}

	@PostMapping("/delete-area")
	public ResponseEntity<StatusOnlyResponse> deleteArea(@RequestBody DeleteAreaRequest dto) {
		if (!coordinateService.deleteArea(dto)) {
			return ResponseEntity.
				status(400)
				.body(StatusOnlyResponse.of(400));
		}

		return ResponseEntity
			.ok(StatusOnlyResponse.of(200));
	}

	// TODO 키 값이 아이디인데 DTO로 리턴하기는 어려울 것 같음, 해결할 방법 찾기
	@PostMapping("/read-areas")
	public ResponseEntity<Map<String, ReadAreaResponse>> readAreas(@RequestBody GetChildNameRequest dto) {
		HashMap<String, ReadAreaResponse> result = new HashMap<>();

		// 1. child에 저장되어 있는 coordinate 불러오기
		ArrayList<Coordinate> coordinates = coordinateService.readAreasByChild(dto.getChildName());

		// 2. responseDTO로 변경
		for (Coordinate coordinate : coordinates) {
			result.put(coordinate.getCoordinateId() + "",
				ReadAreaResponse.builder()
					.isLiving(coordinate.isLivingArea() + "")
					.XOfPointA(coordinate.getXOfNorthEast())
					.YOfPointA(coordinate.getYOfNorthEast())
					.XOfPointB(coordinate.getXOfNorthWest())
					.YOfPointB(coordinate.getYOfNorthWest())
					.XOfPointC(coordinate.getXOfSouthWest())
					.YOfPointC(coordinate.getYOfSouthWest())
					.XOfPointD(coordinate.getXOfSouthEast())
					.YOfPointD(coordinate.getYOfSouthEast())
					.build()
			);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/area-detail")
	public ResponseEntity<Map<String, AreaDetailResponse>> areaDetail(@RequestBody AreaDetailRequest dto) {
		Map<String, AreaDetailResponse> result = new HashMap<>();

		Coordinate coordinate = coordinateService.findAreaById(dto.getAreaId());
		if (coordinate == null) {
			return ResponseEntity.status(400).build();
		}

		result.put(coordinate.getCoordinateId() + "",
			AreaDetailResponse.builder()
				.isLiving(coordinate.isLivingArea() + "")
				.XOfPointA(coordinate.getXOfNorthEast())
				.YOfPointA(coordinate.getYOfNorthEast())
				.XOfPointB(coordinate.getXOfNorthWest())
				.YOfPointB(coordinate.getYOfNorthWest())
				.XOfPointC(coordinate.getXOfSouthWest())
				.YOfPointC(coordinate.getYOfSouthWest())
				.XOfPointD(coordinate.getXOfSouthEast())
				.YOfPointD(coordinate.getYOfSouthEast())
				.build()
		);

		return ResponseEntity.ok().body(result);
	}
}
