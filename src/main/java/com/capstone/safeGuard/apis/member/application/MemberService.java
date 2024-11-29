package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.FindMemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.comment.infrastructure.CommentRepository;
import com.capstone.safeGuard.domain.file.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.domain.file.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Authority;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.ChildBattery;
import com.capstone.safeGuard.domain.member.domain.EmailAuthCode;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.MemberBattery;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.ChildBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.EmailAuthCodeRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

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
	private final MailService mailService;
	private final EmailAuthCodeRepository emailAuthCodeRepository;

	private static final int emailAuthCodeDuration = 1800; // 30 * 60 * 1000 == 30분
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

	@Transactional
	public Member memberLogin(LoginRequest dto) {
		// 존재하는 멤버인가
		Optional<Member> foundMember = memberRepository.findById(dto.editTextID());
		if (foundMember.isEmpty()) {
			return null;
		}

		// ID와 PW가 일치하는가
		Member member = findMemberWithAuthenticate(foundMember.get(), dto.editTextPW());
		if (member == null) {
			return null;
		}

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

	public Member findMemberWithAuthenticate(Member findMember, String rawPassword) {
		if (passwordEncoder.matches(rawPassword, findMember.getPassword())) {
			return findMember;
		}
		return null;
	}

	public Child childLogin(LoginRequest dto) {
		Optional<Child> findChild = childRepository.findBychildName(dto.editTextID());

		return findChild
			.map(child -> findChildWithAuthenticate(child, dto.editTextPW()))
			.orElse(null);

	}

	private Child findChildWithAuthenticate(Child findChild, String rawPassword) {
		if (passwordEncoder.matches(rawPassword, findChild.getChildPassword())) {
			return findChild;
		}
		return null;
	}

	public Boolean signup(SignUpRequest dto) {
		Optional<Member> findMember = memberRepository.findById(dto.inputID());
		if (findMember.isPresent()) {
			return false;
		}

		String email = dto.inputEmail();
		if (checkEmailDuplicate(email)) {
			log.info("Email Duplicate");
			return false;
		}

		Member member = new Member();
		member.setMemberId(dto.inputID());
		member.setEmail(dto.inputEmail());
		member.setName(dto.inputName());
		String encodedPassword = passwordEncoder.encode(dto.inputPW());
		member.setPassword(encodedPassword);
		member.setAuthority(Authority.ROLE_MEMBER);
		member.setFcmToken(dto.fcmToken());
		memberRepository.save(member);

		return true;
	}

	public boolean checkEmailDuplicate(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Transactional
	public Boolean childSignUp(ChildRegisterRequest childDto) {
		Optional<Child> findChild = childRepository.findBychildName(childDto.childName());
		if (findChild.isPresent()) {
			return false;
		}
		log.info(childDto.memberId());
		log.info(childDto.childName());
		log.info(childDto.childPassword());

		Child child = new Child();
		child.setChildName(childDto.childName());
		String encodedPassword = passwordEncoder.encode(childDto.childPassword());
		child.setChildPassword(encodedPassword);
		child.setAuthority(Authority.ROLE_CHILD);
		child.setLastStatus("일반구역");
		childRepository.save(child);

		// member child 연결
		String memberId = childDto.memberId();
		Optional<Member> findMember = memberRepository.findById(memberId);
		if (findMember.isEmpty()) {
			return false;
		}
		saveParenting(memberId, child);

		return true;
	}

	@Transactional
	public void saveParenting(String memberId, Child child) {
		// 부모와 자식 엔티티의 ID를 사용하여 엔티티 객체를 가져옴
		Optional<Member> parent = memberRepository.findById(memberId);

		if (parent.isEmpty() || child == null) {
			// 부모나 자식이 존재하지 않는 경우 처리
			return;
		}

		// Parenting 엔티티 생성
		Parenting parenting = new Parenting();
		parenting.setParent(parent.get());
		parenting.setChild(child);

		// Parenting 엔티티 저장
		parentingRepository.save(parenting);
	}

	@Transactional
	public Boolean addHelper(MemberRegisterRequest memberRegisterRequest) {
		Helping helping = new Helping();
		String childName = memberRegisterRequest.childName();
		String memberId = memberRegisterRequest.parentId();

		Child selectedChild = childRepository.findBychildName(childName)
			.orElse(null);
		if (selectedChild == null) {
			return false;
		}
		Optional<Member> findMember = memberRepository.findById(memberId);
		if (findMember.isEmpty()) {
			return false;
		}

		List<Helping> foundHelpingList = helpingRepository.findAllByHelper(findMember.get());
		for (Helping foundHelping : foundHelpingList) {
			if (foundHelping.getChild().equals(selectedChild)) {
				return false;
			}
		}

		helping.setHelper(findMember.get());
		helping.setChild(selectedChild);

		helpingRepository.save(helping);

		return true;
	}

	@Transactional
	public Boolean memberRemove(String memberId) {
		Optional<Member> member = memberRepository.findById(memberId);
		if (member.isEmpty()) {
			return false;
		}

		ArrayList<String> childNameList = findChildList(memberId);
		if (!(childNameList == null)) {
			for (String childName : childNameList) {
				childRemove(childName);
			}
		}

		List<Comment> commented = member.get().getCommented();
		if (!(commented == null)) {
			commentRepository.deleteAll(commented);
		}

		List<Helping> helpingList = member.get().getHelpingList();
		if (!(helpingList == null)) {
			for (Helping helping : helpingList) {
				ArrayList<Confirm> confirmArrayList = confirmRepository.findAllByHelpingId(helping);
				if (!confirmArrayList.isEmpty()) {
					confirmRepository.deleteAll(confirmArrayList);
				}
			}
			helpingRepository.deleteAll(helpingList);
		}

		List<Parenting> parentingList = member.get().getParentingList();
		if (!(parentingList == null)) {
			parentingRepository.deleteAll(parentingList);
		}

		memberFileRepository.findByMember(member.get())
			.ifPresent(memberFileRepository::delete);

		List<Emergency> emergencyList = emergencyRepository.findAllBySenderId(member.get());
		if (!(emergencyList == null)) {
			for (Emergency emergency : emergencyList) {
				List<EmergencyReceiver> emergencyReceiverList = emergencyReceiverRepository.findAllByEmergency(emergency);
				if (!emergencyReceiverList.isEmpty()) {
					emergencyReceiverRepository.deleteAll(emergencyReceiverList);
				}
			}
			emergencyRepository.deleteAll(emergencyList);
		}

		Optional<MemberBattery> memberBattery = memberBatteryRepository.findByMemberId(member.get());
		memberBattery.ifPresent(battery -> memberBatteryRepository.deleteById(battery.getMemberBatteryId()));

		memberRepository.delete(member.get());
		return true;
	}

	@Transactional
	public Boolean childRemove(String childName) {
		Child selectedChild = childRepository.findBychildName(childName)
			.orElse(null);
		if (selectedChild == null) {
			return false;
		}

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
		Helping helping = helpingRepository.findByHelper_MemberIdAndChild_ChildName(dto.memberId(), dto.childName());
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
		Optional<Member> loginedMember = memberRepository.findById(memberId);
		if (loginedMember.isEmpty()) {
			return null;
		}
		Member member = loginedMember.get();

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

	public boolean sendCodeToEmail(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		if (foundMember.isEmpty()) {
			return false;
		}

		String address = foundMember.get().getEmail();
		String title = "SafeGuard 이메일 인증 번호";
		String authCode = createCode();

		mailService.sendEmail(address, title, authCode);
		Optional<EmailAuthCode> foundCode = emailAuthCodeRepository.findById(memberId);
		foundCode.ifPresent(emailAuthCodeRepository::delete);
		emailAuthCodeRepository.save(new EmailAuthCode(address, authCode, LocalDateTime.now()));
		return true;
	}

	private String createCode() {
		int length = 6;
		Random random = new Random();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			builder.append(random.nextInt(10));
		}

		return builder.toString();
	}


	public boolean verifiedCode(String memberId, String authCode) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		if (foundMember.isEmpty()) {
			return false;
		}

		Optional<EmailAuthCode> foundCode = emailAuthCodeRepository.findById(foundMember.get().getEmail());
		if (foundCode.isEmpty()) {
			return false;
		}

		if (Duration.between(foundCode.get().getCreatedAt(), LocalDateTime.now()).getSeconds()
			> emailAuthCodeDuration) {
			return false;
		}

		return authCode.equals(foundCode.get().getAuthCode());
	}

	@Transactional
	public boolean resetMemberPassword(ResetPasswordRequest dto) {
		Optional<Member> foundMember = memberRepository.findById(dto.id());

		if (foundMember.isEmpty()) {
			return false;
		}

		foundMember.get().setPassword(passwordEncoder.encode(dto.newPassword()));
		return true;
	}

	@Transactional
	public ArrayList<String> findChildList(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		if (foundMember.isEmpty()) {
			return null;
		}

		List<Parenting> parentingList = foundMember.get().getParentingList();
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
		Child foundChild = childRepository.findBychildName(dto.id())
			.orElse(null);

		if (foundChild == null) {
			return false;
		}

		foundChild.setChildPassword(passwordEncoder.encode(dto.newPassword()));
		return true;
	}

	public ArrayList<Member> findAllMember() {
		return new ArrayList<>(memberRepository.findAll());
	}

	@Transactional
	public boolean updateMemberCoordinate(String id, double latitude, double longitude) {
		Optional<Member> foundMember = memberRepository.findById(id);
		if (foundMember.isEmpty()) {
			return false;
		}

		foundMember.get().setLatitude(latitude);
		foundMember.get().setLongitude(longitude);

		return true;
	}

	// 해당 메소드에서 id는 child의 name이다.
	@Transactional
	public boolean updateChildCoordinate(String id, double latitude, double longitude) {
		Child foundChild = childRepository.findBychildName(id)
			.orElse(null);
		if (foundChild == null) {
			return false;
		}

		foundChild.setLatitude(latitude);
		foundChild.setLongitude(longitude);

		return true;
	}

	@Transactional
	public Map<String, Double> getMemberCoordinate(String id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Member not found"));

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put("latitude", member.getLatitude());
		coordinates.put("longitude", member.getLongitude());

		return coordinates;
	}

	@Transactional
	public Map<String, Double> getChildCoordinate(String id) {
		Child foundChild = childRepository.findBychildName(id)
			.orElseThrow(() -> new RuntimeException("Child not found"));

		Map<String, Double> coordinates = new HashMap<>();
		coordinates.put("latitude", foundChild.getLatitude());
		coordinates.put("longitude", foundChild.getLongitude());

		return coordinates;
	}


	public boolean isPresent(String id, boolean flag) {
		if (flag) {
			return memberRepository.findById(id).isPresent();
		}
		return childRepository.findBychildName(id).isPresent();
	}

	@Transactional
	public Child findChildByChildName(String childName) {
		return childRepository.findBychildName(childName)
			.orElse(null);
	}


	@Transactional
	public ArrayList<String> findHelpingList(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		if (foundMember.isEmpty()) {
			return null;
		}

		List<Helping> helpingList = foundMember.get().getHelpingList();
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
		Optional<Member> foundMember = memberRepository.findById(memberId);
		if (foundMember.isEmpty()) {
			return false;
		}

		Child foundChild = childRepository.findBychildName(childName)
			.orElse(null);
		if (foundChild == null) {
			return false;
		}

		List<Parenting> foundParentingList = parentingRepository.findAllByParent(foundMember.get());
		for (Parenting foundParenting : foundParentingList) {
			if (foundParenting.getChild().equals(foundChild)) {
				return false;
			}
		}

		saveParenting(memberId, foundChild);

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

	public String getNicknameById(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		return foundMember.map(Member::getName).orElse(null);
	}
}