package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.ChildBattery;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.MemberBattery;
import com.capstone.safeGuard.domain.member.infrastructure.ChildBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class BatteryService {
	private final ChildBatteryRepository childBatteryRepository;
	private final MemberBatteryRepository memberBatteryRepository;
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	// Redis에 저장하는 키의 접두사
	private static final String MEMBER_BATTERY_KEY_PREFIX = "member_battery:";
	private static final String CHILD_BATTERY_KEY_PREFIX = "child_battery:";

	private static final int MIN_TIMEOUT = 60;
	private static final String BATTERY_KEY = "battery";

	@Transactional
	public void initChildBattery(Child foundChild, int battery) {
		childBatteryRepository.save(
			ChildBattery.builder()
				.childName(foundChild)
				.batteryValue(battery)
				.build()
		);
	}

	@Transactional
	public void initMemberBattery(Member member, int battery) {
		memberBatteryRepository.save(
			MemberBattery.builder()
				.memberId(member)
				.batteryValue(battery).
				build()
		);
	}

	@Transactional
	public void setChildBattery(String id, int battery) {
		Map<String, Integer> batteryMap = new HashMap<>();
		batteryMap.put(BATTERY_KEY, battery);

		redisTemplate.opsForHash().putAll(CHILD_BATTERY_KEY_PREFIX + id, batteryMap);
		redisTemplate.expire(CHILD_BATTERY_KEY_PREFIX + id, Duration.ofMinutes(MIN_TIMEOUT));
	}

	@Transactional
	public void setMemberBattery(String id, int battery) {
		Map<String, Integer> batteryMap = new HashMap<>();
		batteryMap.put(BATTERY_KEY, battery);

		redisTemplate.opsForHash().putAll(MEMBER_BATTERY_KEY_PREFIX + id, batteryMap);
		redisTemplate.expire(MEMBER_BATTERY_KEY_PREFIX + id, Duration.ofMinutes(MIN_TIMEOUT));
	}

	/**
	 * 스케줄링: Redis 데이터를 MySQL로 주기적으로 동기화
	 */
	@Transactional
	@Scheduled(cron = "0 0,30 * * * *") // 30분 간격
	public void syncCoordinatesToDatabaseBattery() {
		syncMemberBattery();
		syncChildBattery();
	}

	@Transactional
	public void syncMemberBattery() {
		Set<String> keys = redisTemplate.keys(MEMBER_BATTERY_KEY_PREFIX + "*");

		if (keys.isEmpty()) return;

		List<String> ids = keys.stream()
			.map(key -> key.replace(MEMBER_BATTERY_KEY_PREFIX, ""))
			.collect(Collectors.toList());

		List<Member> members = memberRepository.findAllById(ids);
		Map<String, Member> memberMap = members.stream()
			.collect(Collectors.toMap(Member::getMemberId, member -> member));

		List<MemberBattery> existingBatteries = memberBatteryRepository.findByMemberIdIn(members);
		Map<String, MemberBattery> batteryMap = existingBatteries.stream()
			.collect(Collectors.toMap(battery -> battery.getMemberId().getMemberId(), battery -> battery));

		for (String key : keys) {
			String id = key.replace(MEMBER_BATTERY_KEY_PREFIX, "");
			Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(key);

			if (!cachedCoordinates.isEmpty()) {
				int battery = (Integer) cachedCoordinates.get(BATTERY_KEY);

				Member member = memberMap.get(id);
				if (member != null) {
					MemberBattery batteryEntity = batteryMap.get(id);

					if (batteryEntity == null) {
						initMemberBattery(member, battery);
					} else {
						batteryEntity.setBatteryValue(battery);
					}
				}

				redisTemplate.delete(key);
			}
		}
	}

	@Transactional
	public void syncChildBattery() {
		Set<String> keys = redisTemplate.keys(CHILD_BATTERY_KEY_PREFIX + "*");

		if (keys.isEmpty()) return;

		List<String> names = keys.stream()
			.map(key -> key.replace(CHILD_BATTERY_KEY_PREFIX, ""))
			.collect(Collectors.toList());

		List<Child> children = childRepository.findByChildNameIn(names);
		Map<String, Child> childMap = children.stream()
			.collect(Collectors.toMap(Child::getChildName, child -> child));

		List<ChildBattery> existingBatteries = childBatteryRepository.findByChildNameIn(children);
		Map<String, ChildBattery> batteryMap = existingBatteries.stream()
			.collect(Collectors.toMap(battery -> battery.getChildName().getChildName(), battery -> battery));

		for (String key : keys) {
			String name = key.replace(CHILD_BATTERY_KEY_PREFIX, "");
			Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(key);

			if (!cachedCoordinates.isEmpty()) {
				int battery = (Integer) cachedCoordinates.get(BATTERY_KEY);

				Child child = childMap.get(name);
				if (child != null) {
					ChildBattery batteryEntity = batteryMap.get(name);

					if (batteryEntity == null) {
						initChildBattery(child, battery);
					} else {
						batteryEntity.setBatteryValue(battery);
					}
				}

				redisTemplate.delete(key);
			}
		}
	}


	@Transactional
	public int getChildBattery(String id) {
		Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(CHILD_BATTERY_KEY_PREFIX + id);
		if (!cachedCoordinates.isEmpty()) {
			// Redis에 데이터가 있는 경우
			return (Integer) cachedCoordinates.get(BATTERY_KEY);
		}

		Child foundChild = findChildByName(id);
		ChildBattery childBattery = childBatteryRepository.findByChildName(foundChild)
			.orElseThrow(() -> new RuntimeException("ChildBattery not found"));
		return childBattery.getBatteryValue();
	}

	@Transactional
	public int getMemberBattery(String id) {
		Map<Object, Object> cachedCoordinates = redisTemplate.opsForHash().entries(MEMBER_BATTERY_KEY_PREFIX + id);
		if (!cachedCoordinates.isEmpty()) {
			// Redis에 데이터가 있는 경우
			return (Integer) cachedCoordinates.get(BATTERY_KEY);
		}

		Member foundMember = findMemberById(id);
		MemberBattery memberBattery = memberBatteryRepository.findByMemberId(foundMember)
			.orElseThrow(() -> new RuntimeException("MemberBattery not found"));
		return memberBattery.getBatteryValue();
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
