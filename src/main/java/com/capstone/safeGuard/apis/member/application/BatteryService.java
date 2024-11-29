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

@Service
@RequiredArgsConstructor
public class BatteryService {
	private final ChildBatteryRepository childBatteryRepository;
	private final MemberBatteryRepository memberBatteryRepository;
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;

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
	public void setChildBattery(String id, int battery) {
		Child foundChild = childRepository.findByChildName(id)
			.orElseThrow(() -> new RuntimeException("Child not found"));

		ChildBattery foundBattery = childBatteryRepository.findByChildName(foundChild)
			.orElse(null);

		if (foundBattery == null) {
			initChildBattery(foundChild, battery);
			return;
		}

		foundBattery.setBatteryValue(battery);
	}

	@Transactional
	public ChildBattery getChildBattery(String id) {
		Child foundChild = childRepository.findByChildName(id)
			.orElseThrow(() -> new RuntimeException("Child not found"));

		return childBatteryRepository.findByChildName(foundChild)
			.orElseThrow(() -> new RuntimeException("ChildBattery not found"));
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
	public void setMemberBattery(String id, int battery) {
		Member foundMember = memberRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Member not found"));

		MemberBattery foundBattery = memberBatteryRepository.findByMemberId(foundMember)
			.orElse(null);

		if (foundBattery == null) {
			initMemberBattery(foundMember, battery);
			return;
		}

		foundBattery.setBatteryValue(battery);
	}

	@Transactional
	public MemberBattery getMemberBattery(String id) {
		Member foundMember = memberRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Member not found"));

		return memberBatteryRepository.findByMemberId(foundMember)
			.orElseThrow(() -> new RuntimeException("MemberBattery not found"));
	}
}
