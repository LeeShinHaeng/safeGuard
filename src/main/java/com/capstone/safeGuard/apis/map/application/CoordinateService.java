package com.capstone.safeGuard.apis.map.application;

import com.capstone.safeGuard.apis.map.presentation.request.coordinate.AddAreaRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.DeleteAreaRequest;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoordinateService {
	private final CoordinateRepository coordinateRepository;
	private final ChildRepository childRepository;
	private final MemberRepository memberRepository;

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

	@Transactional
	public void updateMemberCoordinate(String id, double latitude, double longitude) {
		Member foundMember = findMemberById(id);
		foundMember.setLatitude(latitude);
		foundMember.setLongitude(longitude);
	}

	@Transactional
	public void updateChildCoordinate(String name, double latitude, double longitude) {
		Child foundChild = findChildByName(name);

		foundChild.setLatitude(latitude);
		foundChild.setLongitude(longitude);
	}

	@Transactional
	public Map<String, Double> getMemberCoordinate(String id) {
		Member member = findMemberById(id);

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put("latitude", member.getLatitude());
		coordinates.put("longitude", member.getLongitude());

		return coordinates;
	}

	@Transactional
	public Map<String, Double> getChildCoordinate(String id) {
		Child foundChild = findChildByName(id);

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put("latitude", foundChild.getLatitude());
		coordinates.put("longitude", foundChild.getLongitude());

		return coordinates;
	}

	public Member findMemberById(String memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new RuntimeException("Member not found"));
	}

	private Child findChildByName(String name) {
		return childRepository.findByChildName(name)
			.orElseThrow(() -> new RuntimeException("Child not found"));
	}
}
