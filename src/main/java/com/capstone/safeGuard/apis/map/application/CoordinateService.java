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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class CoordinateService {
	private final CoordinateRepository coordinateRepository;
	private final ChildRepository childRepository;
	private final MemberRepository memberRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	// Redis에 저장하는 키의 접두사
	private static final String MEMBER_COORDINATES_KEY_PREFIX = "member:";
	private static final String CHILD_COORDINATES_KEY_PREFIX = "child:";

	private static final int MIN_TIMEOUT = 60;
	private static final String LATITUDE_KEY = "latitude";
	private static final String LONGITUDE_KEY = "longitude";

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
		if (!foundChild.equals(foundCoordinate.getChild())) {
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
		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put(LATITUDE_KEY, latitude);
		coordinates.put(LONGITUDE_KEY, longitude);

		redisTemplate.opsForHash().putAll(MEMBER_COORDINATES_KEY_PREFIX + id, coordinates);
		redisTemplate.expire(MEMBER_COORDINATES_KEY_PREFIX + id, Duration.ofMinutes(MIN_TIMEOUT));
	}

	@Transactional
	public void updateChildCoordinate(String name, double latitude, double longitude) {
		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put(LATITUDE_KEY, latitude);
		coordinates.put(LONGITUDE_KEY, longitude);

		redisTemplate.opsForHash().putAll(CHILD_COORDINATES_KEY_PREFIX + name, coordinates);
		redisTemplate.expire(CHILD_COORDINATES_KEY_PREFIX + name, Duration.ofMinutes(MIN_TIMEOUT));
	}

	/**
	 * 스케줄링: Redis 데이터를 MySQL로 주기적으로 동기화
	 */
	@Transactional
	@Scheduled(cron = "0 0,30 * * * *") // 30분 간격
	public void syncCoordinatesToDatabase() {
		syncMemberCoordinates();
		syncChildCoordinates();
	}

	private void syncMemberCoordinates() {
		Set<String> keys = redisTemplate.keys(MEMBER_COORDINATES_KEY_PREFIX + "*");

		if (keys.isEmpty()) {
			return;
		}
		List<String> ids = keys.stream()
			.map(key -> key.replace(MEMBER_COORDINATES_KEY_PREFIX, ""))
			.collect(Collectors.toList());

		List<Member> members = memberRepository.findAllById(ids);
		Map<String, Member> memberMap = members.stream()
			.collect(Collectors.toMap(Member::getMemberId, member -> member));

		for (String key : keys) {
			String id = key.replace(MEMBER_COORDINATES_KEY_PREFIX, "");
			Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(key);

			if (!cachedCoordinates.isEmpty()) {
				Double latitude = (Double) cachedCoordinates.get(LATITUDE_KEY);
				Double longitude = (Double) cachedCoordinates.get(LONGITUDE_KEY);

				Member member = memberMap.get(id);
				if (member != null) {
					member.setLatitude(latitude);
					member.setLongitude(longitude);
				}

				redisTemplate.delete(key); // Redis 키 삭제
			}
		}
	}

	private void syncChildCoordinates() {
		Set<String> keys = redisTemplate.keys(CHILD_COORDINATES_KEY_PREFIX + "*");

		if (keys.isEmpty()) {
			return;
		}
		List<String> names = keys.stream()
			.map(key -> key.replace(CHILD_COORDINATES_KEY_PREFIX, ""))
			.collect(Collectors.toList());

		List<Child> children = childRepository.findByChildNameIn(names);
		Map<String, Child> childMap = children.stream()
			.collect(Collectors.toMap(Child::getChildName, child -> child));

		for (String key : keys) {
			String name = key.replace(CHILD_COORDINATES_KEY_PREFIX, "");
			Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(key);

			if (!cachedCoordinates.isEmpty()) {
				Double latitude = (Double) cachedCoordinates.get(LATITUDE_KEY);
				Double longitude = (Double) cachedCoordinates.get(LONGITUDE_KEY);

				Child child = childMap.get(name);
				if (child != null) {
					child.setLatitude(latitude);
					child.setLongitude(longitude);
				}

				redisTemplate.delete(key); // Redis 키 삭제
			}
		}
	}


	@Transactional
	public Map<String, Double> getMemberCoordinate(String id) {
		Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(MEMBER_COORDINATES_KEY_PREFIX + id);
		if (!cachedCoordinates.isEmpty()) {
			// Redis에 데이터가 있는 경우
			Map<String, Double> coordinates = new HashMap<>();
			coordinates.put(LATITUDE_KEY, (Double) cachedCoordinates.get(LATITUDE_KEY));
			coordinates.put(LONGITUDE_KEY, (Double) cachedCoordinates.get(LONGITUDE_KEY));
			return coordinates;
		}

		Member member = findMemberById(id);

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put(LATITUDE_KEY, member.getLatitude());
		coordinates.put(LONGITUDE_KEY, member.getLongitude());

		redisTemplate.opsForHash().putAll(MEMBER_COORDINATES_KEY_PREFIX + id, coordinates);
		redisTemplate.expire(MEMBER_COORDINATES_KEY_PREFIX + id, Duration.ofMinutes(MIN_TIMEOUT));

		return coordinates;
	}

	@Transactional
	public Map<String, Double> getChildCoordinate(String id) {
		Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(CHILD_COORDINATES_KEY_PREFIX + id);
		if (!cachedCoordinates.isEmpty()) {
			// Redis에 데이터가 있는 경우
			Map<String, Double> coordinates = new HashMap<>();
			coordinates.put(LATITUDE_KEY, (Double) cachedCoordinates.get(LATITUDE_KEY));
			coordinates.put(LONGITUDE_KEY, (Double) cachedCoordinates.get(LONGITUDE_KEY));
			return coordinates;
		}

		Child foundChild = findChildByName(id);

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put(LATITUDE_KEY, foundChild.getLatitude());
		coordinates.put(LONGITUDE_KEY, foundChild.getLongitude());

		redisTemplate.opsForHash().putAll(CHILD_COORDINATES_KEY_PREFIX + id, coordinates);
		redisTemplate.expire(CHILD_COORDINATES_KEY_PREFIX + id, Duration.ofMinutes(MIN_TIMEOUT));

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
