package com.capstone.safeGuard.apis.map.application;

import com.capstone.safeGuard.apis.map.presentation.request.coordinate.AddAreaRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.DeleteAreaRequest;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoordinateService {
	private final CoordinateRepository coordinateRepository;
	private final ChildRepository childRepository;

	@Transactional
	public Long addForbiddenArea(AddAreaRequest addAreaRequest) {
		Child foundChild = childRepository.findByChildName(addAreaRequest.childName())
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		Coordinate coordinate = addAreaRequest.dtoToDomain(foundChild, false);
		// child와 coordinate에 저장
		foundChild.getForbiddenAreas().add(coordinate);
		coordinateRepository.save(coordinate);

		log.info("addForbiddenArea 성공 ");
		return coordinate.getCoordinateId();
	}

	@Transactional
	public Long addLivingArea(AddAreaRequest addAreaRequest) {
		log.info("addLivingArea 도착");
		Child foundChild = childRepository.findByChildName(addAreaRequest.childName())
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		Coordinate coordinate = addAreaRequest.dtoToDomain(foundChild, true);

		// child와 coordinate에 저장
		log.info(addAreaRequest.xOfPointA() + " = " + coordinate.getXOfSouthEast());
		foundChild.getLivingAreas().add(coordinate);
		coordinateRepository.save(coordinate);

		log.info("addLivingArea 성공 ");
		return coordinate.getCoordinateId();
	}

	@Transactional
	public void deleteArea(DeleteAreaRequest dto) {
		String areaID = dto.areaID();
		String childName = dto.childName();

		log.info("deleteArea 시작");
		Child foundChild = childRepository.findByChildName(childName)
			.orElseThrow(() -> new RuntimeException("No Such Child"));
		Coordinate foundCoordinate = findAreaById(areaID);
		if (! foundChild.equals(foundCoordinate.getChild())) {
            throw new RuntimeException("Not Match: Child - Coordinate");
		}

		coordinateRepository.delete(foundCoordinate);
	}

	public ArrayList<Coordinate> readAreasByChild(String childName) {
		Child foundChild = childRepository.findByChildName(childName)
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		return coordinateRepository.findAllByChild(foundChild);
	}

	public Coordinate findAreaById(String areaId) {
		return coordinateRepository.findById(Long.parseLong(areaId))
			.orElseThrow(() -> new RuntimeException("No Such Coordinate"));
	}
}
