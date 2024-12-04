package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.FindMemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.comment.infrastructure.CommentRepository;
import com.capstone.safeGuard.domain.file.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.domain.file.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Authority;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.ChildBattery;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.MemberBattery;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.ChildBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.HelpingRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ParentingRepository;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import com.capstone.safeGuard.domain.notice.domain.EmergencyReceiver;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import com.capstone.safeGuard.domain.notice.infrastructure.ConfirmRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyReceiverRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.NoticeRepository;
import com.capstone.safeGuard.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;
	private final ParentingRepository parentingRepository;
	private final HelpingRepository helpingRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private final ConfirmRepository confirmRepository;
	private final CommentRepository commentRepository;
	private final MemberBatteryRepository memberBatteryRepository;
	private final ChildBatteryRepository childBatteryRepository;
	private final EmergencyRepository emergencyRepository;
	private final CoordinateRepository coordinateRepository;
	private final MemberFileRepository memberFileRepository;
	private final ChildFileRepository childFileRepository;
	private final EmergencyReceiverRepository emergencyReceiverRepository;
	private final NoticeRepository noticeRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public Member memberLogin(LoginRequest dto) {
		// 존재하는 멤버인가
		Member member = findMemberById(dto.editTextID());

		// ID와 PW가 일치하는가
		findMemberWithAuthenticate(member, dto.editTextPW());

		// 같은 기기를 사용하는 멤버 모두 FCM 토큰 초기화
		String fcmToken = dto.fcmToken();
		List<Member> existFcmList = memberRepository.findAllByFcmToken(fcmToken);
		if (!existFcmList.isEmpty()) {
			for (Member existFcm : existFcmList) {
				existFcm.setFcmToken(null);
			}
		}
		member.setFcmToken(fcmToken);

		return member;
	}

	public void findMemberWithAuthenticate(Member foundMember, String rawPassword) {
		if (passwordEncoder.matches(rawPassword, foundMember.getPassword())) {
			return;
		}
		throw new RuntimeException("Password not match");
	}

	public Child childLogin(LoginRequest dto) {
		String name = dto.editTextID();
		Child child = findChildByName(name);

		findChildWithAuthenticate(child, dto.editTextPW());

		return child;
	}

	private void findChildWithAuthenticate(Child foundChild, String rawPassword) {
		if (passwordEncoder.matches(rawPassword, foundChild.getChildPassword())) {
			return;
		}
		throw new RuntimeException("Password not match");
	}

	public void signup(SignUpRequest dto) {
		if (memberRepository.existsById(dto.inputID())) {
			throw new RuntimeException("Member already exists");
		}

		String email = dto.inputEmail();
		if (checkEmailDuplicate(email)) {
			log.info("Email Duplicate");
			throw new RuntimeException("Email Duplicate");
		}

		String encoded = passwordEncoder.encode(dto.inputPW());
		Member member = Member.of(dto, encoded);
		memberRepository.save(member);
	}

	public boolean checkEmailDuplicate(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Transactional
	public void childSignUp(ChildRegisterRequest childDto) {
		if (childRepository.existsByChildName(childDto.childName())) {
			throw new RuntimeException("Child already exists");
		}

		Child child = Child.of(childDto, passwordEncoder.encode(childDto.childPassword()));
		childRepository.save(child);

		// member child 연결
		String memberId = childDto.memberId();
		Member member = findMemberById(memberId);

		saveParenting(member, child);
	}

	@Transactional
	public void saveParenting(Member parent, Child child) {
		// Parenting 엔티티 생성
		Parenting parenting = new Parenting();
		parenting.setParent(parent);
		parenting.setChild(child);

		parent.getParentingList().add(parenting);
		child.getParentingList().add(parenting);

		// Parenting 엔티티 저장
		parentingRepository.save(parenting);
	}

	@Transactional
	public Boolean addHelper(MemberRegisterRequest memberRegisterRequest) {
		Helping helping = new Helping();
		String childName = memberRegisterRequest.childName();
		String memberId = memberRegisterRequest.parentId();

		Child selectedChild = findChildByName(childName);
		Member findMember = findMemberById(memberId);

		List<Helping> foundHelpingList = helpingRepository.findAllByHelper(findMember);
		for (Helping foundHelping : foundHelpingList) {
			if (foundHelping.getChild().equals(selectedChild)) {
				return false;
			}
		}

		helping.setHelper(findMember);
		helping.setChild(selectedChild);

		helpingRepository.save(helping);

		return true;
	}

	@Transactional
	public Boolean memberRemove(String memberId) {
		Member member = findMemberById(memberId);

		ArrayList<String> childNameList = findChildList(memberId);
		if (!(childNameList == null)) {
			for (String childName : childNameList) {
				childRemove(childName);
			}
		}

		List<Comment> commented = member.getCommented();
		if (!(commented == null)) {
			commentRepository.deleteAll(commented);
		}

		List<Helping> helpingList = member.getHelpingList();
		if (!(helpingList == null)) {
			for (Helping helping : helpingList) {
				ArrayList<Confirm> confirmArrayList = confirmRepository.findAllByHelpingId(helping);
				if (!confirmArrayList.isEmpty()) {
					confirmRepository.deleteAll(confirmArrayList);
				}
			}
			helpingRepository.deleteAll(helpingList);
		}

		List<Parenting> parentingList = member.getParentingList();
		if (!(parentingList == null)) {
			parentingRepository.deleteAll(parentingList);
		}

		memberFileRepository.findByMember(member)
			.ifPresent(memberFileRepository::delete);

		List<Emergency> emergencyList = emergencyRepository.findAllBySenderId(member);
		if (!(emergencyList == null)) {
			for (Emergency emergency : emergencyList) {
				List<EmergencyReceiver> emergencyReceiverList = emergencyReceiverRepository.findAllByEmergency(emergency);
				if (!emergencyReceiverList.isEmpty()) {
					emergencyReceiverRepository.deleteAll(emergencyReceiverList);
				}
			}
			emergencyRepository.deleteAll(emergencyList);
		}

		Optional<MemberBattery> memberBattery = memberBatteryRepository.findByMemberId(member);
		memberBattery.ifPresent(battery -> memberBatteryRepository.deleteById(battery.getMemberBatteryId()));

		memberRepository.delete(member);
		return true;
	}

	@Transactional
	public Boolean childRemove(String childName) {
		Child selectedChild = findChildByName(childName);

		List<Emergency> emergencyList = emergencyRepository.findAllByChild(selectedChild);
		if (!(emergencyList == null)) {
			for (Emergency emergency : emergencyList) {
				List<EmergencyReceiver> emergencyReceiverList = emergencyReceiverRepository.findAllByEmergency(emergency);
				if (!emergencyReceiverList.isEmpty()) {
					emergencyReceiverRepository.deleteAll(emergencyReceiverList);
				}
			}
			emergencyRepository.deleteAll(emergencyList);
		}

		ArrayList<Coordinate> coordinateArrayList = coordinateRepository.findAllByChild(selectedChild);
		if (!(coordinateArrayList == null)) {
			coordinateRepository.deleteAll(coordinateArrayList);
		}

		ArrayList<Notice> noticeArrayList = noticeRepository.findAllByChild(selectedChild);
		if (!(noticeArrayList == null)) {
			noticeRepository.deleteAll(noticeArrayList);
		}

		ArrayList<Confirm> confirmArrayList = confirmRepository.findAllByChild(selectedChild);
		if (!(confirmArrayList == null)) {
			confirmRepository.deleteAll(confirmArrayList);
		}

		List<Parenting> parentingList = selectedChild.getParentingList();
		if (!(parentingList == null)) {
			parentingRepository.deleteAll(parentingList);
		}

		List<Helping> helpingList = selectedChild.getHelpingList();
		if (!(helpingList == null)) {
			helpingRepository.deleteAll(helpingList);
		}

		childFileRepository.findByChild(selectedChild)
			.ifPresent(childFileRepository::delete);

		Optional<ChildBattery> childBattery = childBatteryRepository.findByChildName(selectedChild);
		childBattery.ifPresent(battery -> childBatteryRepository.deleteById(battery.getChildBatteryId()));

		childRepository.delete(selectedChild);
		return true;
	}

	@Transactional
	public Boolean helperRemove(HelperRemoveRequest dto) {
		Helping helping = helpingRepository.findByHelperMemberIdAndChildChildName(dto.memberId(), dto.childName());
		if (helping == null) {
			return false;
		}

		ArrayList<Confirm> confirmList = confirmRepository.findAllByHelpingId(helping);
		confirmRepository.deleteAll(confirmList);

		helpingRepository.delete(helping);
		return true;
	}

	public boolean logout(String accessToken) {
		jwtService.toBlackList(accessToken);
		return true;
	}

	public List<Child> getChildList(String memberId) {
		Member member = findMemberById(memberId);

		List<Child> childList = new ArrayList<>();
		for (Parenting parenting : member.getParentingList()) {
			if (Objects.equals(parenting.getParent().getMemberId(), member.getMemberId())) {
				childList.add(parenting.getChild());
			}
		}
		return childList;
	}

	public String validateBindingError(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			List<FieldError> errors = bindingResult.getFieldErrors();
			StringBuilder errorMessage = new StringBuilder();
			for (FieldError error : errors) {
				errorMessage.append(error.getDefaultMessage()).append("\n");
			}
			return errorMessage.toString();
		}
		return null;
	}

	public String findMemberId(FindMemberIdRequest dto) {
		Member foundMember = memberRepository.findByEmail(dto.email());

		if (foundMember == null || (!foundMember.getName().equals(dto.name()))) {
			return null;
		}

		return foundMember.getMemberId();
	}


	@Transactional
	public boolean resetMemberPassword(ResetPasswordRequest dto) {
		Member foundMember = findMemberById(dto.id());

		foundMember.setPassword(passwordEncoder.encode(dto.newPassword()));
		return true;
	}

	@Transactional
	public ArrayList<String> findChildList(String memberId) {
		Member foundMember = findMemberById(memberId);

		List<Parenting> parentingList = foundMember.getParentingList();
		if (parentingList.isEmpty()) {
			return null;
		}

		ArrayList<String> childNameList = new ArrayList<>();
		for (Parenting parenting : parentingList) {
			childNameList.add(parenting.getChild().getChildName());
		}

		return childNameList;
	}

	@Transactional
	public boolean resetChildPassword(ResetPasswordRequest dto) {
		Child foundChild = findChildByName(dto.id());
		foundChild.setChildPassword(passwordEncoder.encode(dto.newPassword()));
		return true;
	}

	public ArrayList<Member> findAllMember() {
		return new ArrayList<>(memberRepository.findAll());
	}

	@Transactional
	public void updateMemberCoordinate(String id, double latitude, double longitude) {
		Member foundMember = findMemberById(id);
		foundMember.setLatitude(latitude);
		foundMember.setLongitude(longitude);
	}

	// 해당 메소드에서 id는 child의 name이다.
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


	public boolean isPresent(String id, boolean flag) {
		if (flag) {
			return memberRepository.findById(id).isPresent();
		}
		return childRepository.findByChildName(id).isPresent();
	}

	@Transactional
	public Child findChildByChildName(String childName) {
		return childRepository.findByChildName(childName)
			.orElse(null);
	}


	@Transactional
	public ArrayList<String> findHelpingList(String memberId) {
		Member foundMember = findMemberById(memberId);

		List<Helping> helpingList = foundMember.getHelpingList();
		if (helpingList.isEmpty()) {
			return null;
		}

		ArrayList<String> childNameList = new ArrayList<>();
		for (Helping helping : helpingList) {
			childNameList.add(helping.getChild().getChildName());
		}

		return childNameList;
	}

	public Member findMemberById(String memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new RuntimeException("Member not found"));
	}

	@Transactional
	public boolean addParent(String memberId, String childName) {
		Member foundMember = findMemberById(memberId);

		Child foundChild = childRepository.findByChildName(childName)
			.orElse(null);
		if (foundChild == null) {
			return false;
		}

		List<Parenting> foundParentingList = parentingRepository.findAllByParent(foundMember);
		for (Parenting foundParenting : foundParentingList) {
			if (foundParenting.getChild().equals(foundChild)) {
				return false;
			}
		}

		saveParenting(foundMember, foundChild);

		return true;
	}

	public ArrayList<Member> findAllParentByChild(Child foundChild) {
		List<Parenting> parentingList = foundChild.getParentingList().stream().toList();
		if (parentingList.isEmpty()) {
			return null;
		}

		ArrayList<Member> memberList = new ArrayList<>();
		for (Parenting parenting : parentingList) {
			memberList.add(parenting.getParent());
		}

		return memberList;
	}

	@Transactional
	public boolean updateMemberName(UpdateMemberNameRequest dto) {
		Member foundMember = findMemberById(dto.userID());
		foundMember.setName(dto.nickname());
		return true;
	}

	private Child findChildByName(String name) {
		return childRepository.findByChildName(name)
			.orElseThrow(() -> new RuntimeException("Child not found"));
	}

	public String getNicknameById(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		return foundMember.map(Member::getName).orElse(null);
	}

	public TokenInfo generateTokenOfMember(Member member) {
		Authentication authentication
			= new UsernamePasswordAuthenticationToken(member.getMemberId(), member.getPassword(),
			Collections.singleton(new SimpleGrantedAuthority(Authority.ROLE_MEMBER.toString())));
		return jwtTokenProvider.generateToken(authentication);
	}

	public TokenInfo generateTokenOfChild(Child child) {
		Authentication authentication
			= new UsernamePasswordAuthenticationToken(child.getChildName(), child.getChildPassword(),
			Collections.singleton(new SimpleGrantedAuthority(Authority.ROLE_CHILD.toString())));
		return jwtTokenProvider.generateToken(authentication);
	}
}