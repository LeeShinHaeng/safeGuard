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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatteryService {
	private final ChildBatteryRepository childBatteryRepository;
	private final MemberBatteryRepository memberBatteryRepository;
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;

	@Transactional
	public void initChildBattery(String id, int battery) {
		Child foundChild = childRepository.findByChildName(id)
			.orElse(null);

		childBatteryRepository.save(
			ChildBattery.builder()
				.childName(foundChild)
				.batteryValue(battery)
				.build()
		);
	}

	@Transactional
	public boolean setChildBattery(String id, int battery) {
		Child foundChild = childRepository.findByChildName(id)
			.orElse(null);

		Optional<ChildBattery> foundBattery = childBatteryRepository.findByChildName(foundChild);
		if (foundBattery.isEmpty()) {
			initChildBattery(id, battery);
			return true;
		}

		foundBattery.get().setBatteryValue(battery);
		return true;
	}

	@Transactional
	public ChildBattery getChildBattery(String id) {
		Child foundChild = childRepository.findByChildName(id)
			.orElse(null);

		Optional<ChildBattery> foundBattery = childBatteryRepository.findByChildName(foundChild);
		return foundBattery.orElse(null);
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
	public boolean setMemberBattery(String id, int battery) {
		Optional<Member> foundMember = memberRepository.findById(id);
		if (foundMember.isEmpty()) {
			return false;
		}

		Optional<MemberBattery> foundBattery = memberBatteryRepository.findByMemberId(foundMember.get());
		if (foundBattery.isEmpty()) {
			initMemberBattery(foundMember.get(), battery);
			return true;
		}

		foundBattery.get().setBatteryValue(battery);
		return true;
	}

	@Transactional
	public MemberBattery getMemberBattery(String id) {
		Optional<Member> foundMember = memberRepository.findById(id);
		if (foundMember.isEmpty()) {
			return null;
		}

		Optional<MemberBattery> foundBattery = memberBatteryRepository.findByMemberId(foundMember.get());
		return foundBattery.orElse(null);
	}
}
