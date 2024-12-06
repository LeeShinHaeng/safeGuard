package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.domain.file.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.ChildBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import com.capstone.safeGuard.domain.notice.infrastructure.ConfirmRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChildService {
	private final ChildRepository childRepository;
	private final PasswordEncoder passwordEncoder;

	private final ConfirmRepository confirmRepository;
	private final ChildBatteryRepository childBatteryRepository;
	private final EmergencyRepository emergencyRepository;
	private final CoordinateRepository coordinateRepository;
	private final ChildFileRepository childFileRepository;
	private final NoticeRepository noticeRepository;
	private final MemberUtil memberUtil;

	@Transactional
	public Child childLogin(LoginRequest dto) {
		String name = dto.editTextID();
		Child child = memberUtil.findChildByName(name);

		findChildWithAuthenticate(child, dto.editTextPW());

		return child;
	}

	@Transactional
	public void findChildWithAuthenticate(Child foundChild, String rawPassword) {
		if (!passwordEncoder.matches(rawPassword, foundChild.getChildPassword())) {
			throw new RuntimeException("Password not match");
		}
	}

	@Transactional
	public void childSignUp(ChildRegisterRequest childDto) {
		if (childRepository.existsByChildName(childDto.childName())) {
			throw new RuntimeException("Child already exists");
		}

		Child child = Child.of(childDto, passwordEncoder.encode(childDto.childPassword()));
		childRepository.save(child);

		String memberId = childDto.memberId();
		Member member = memberUtil.findMemberById(memberId);

		memberUtil.saveParenting(member, child);
	}

	@Transactional
	public void childRemove(String childName) {
		Child child = memberUtil.findChildByName(childName);
		cascadeChildRemove(child);
		childRepository.delete(child);
	}

	@Transactional
	public void cascadeChildRemove(Child child) {
		ArrayList<Coordinate> coordinateArrayList = coordinateRepository.findAllByChild(child);
		if (coordinateArrayList != null) {
			coordinateRepository.deleteAll(coordinateArrayList);
		}

		ArrayList<Notice> noticeArrayList = noticeRepository.findAllByChild(child);
		if (noticeArrayList != null) {
			noticeRepository.deleteAll(noticeArrayList);
		}

		ArrayList<Confirm> confirmArrayList = confirmRepository.findAllByChild(child);
		if (confirmArrayList != null) {
			confirmRepository.deleteAll(confirmArrayList);
		}

		memberUtil.deleteParentingList(child.getParentingList());
		memberUtil.deleteHelpingList(child.getHelpingList());
		memberUtil.deleteEmergencyList(emergencyRepository.findAllByChild(child));

		childFileRepository.findByChild(child)
			.ifPresent(childFileRepository::delete);
		childBatteryRepository.findByChildName(child)
			.ifPresent(battery -> childBatteryRepository.deleteById(battery.getChildBatteryId()));
	}

	public List<Child> getChildList(String memberId) {
		Member member = memberUtil.findMemberById(memberId);

		List<Child> childList = new ArrayList<>();
		for (Parenting parenting : member.getParentingList()) {
			if (Objects.equals(parenting.getParent().getMemberId(), member.getMemberId())) {
				childList.add(parenting.getChild());
			}
		}
		return childList;
	}

	@Transactional
	public void resetChildPassword(ResetPasswordRequest dto) {
		Child foundChild = memberUtil.findChildByName(dto.id());
		foundChild.setChildPassword(passwordEncoder.encode(dto.newPassword()));
	}

}
