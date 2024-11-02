package com.capstone.safeGuard.member.apis.application;

import com.capstone.safeGuard.comment.domain.domain.Comment;
import com.capstone.safeGuard.comment.domain.infrastructure.CommentRepository;
import com.capstone.safeGuard.map.domain.domain.Coordinate;
import com.capstone.safeGuard.member.apis.presentation.request.findidandresetpw.FindMemberIdDTO;
import com.capstone.safeGuard.member.apis.presentation.request.findidandresetpw.ResetPasswordDTO;
import com.capstone.safeGuard.file.domain.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.file.domain.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.map.domain.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.AddMemberDto;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.ChildSignUpRequestDTO;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.DrawHelperDTO;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.LoginRequestDTO;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.SignUpRequestDTO;
import com.capstone.safeGuard.member.apis.presentation.request.signupandlogin.UpdateMemberNameDTO;
import com.capstone.safeGuard.member.domain.domain.Authority;
import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.member.domain.domain.ChildBattery;
import com.capstone.safeGuard.member.domain.domain.EmailAuthCode;
import com.capstone.safeGuard.member.domain.domain.Helping;
import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.member.domain.domain.MemberBattery;
import com.capstone.safeGuard.member.domain.domain.Parenting;
import com.capstone.safeGuard.member.domain.infrastructure.ChildBatteryRepository;
import com.capstone.safeGuard.member.domain.infrastructure.ChildRepository;
import com.capstone.safeGuard.member.domain.infrastructure.EmailAuthCodeRepository;
import com.capstone.safeGuard.member.domain.infrastructure.HelpingRepository;
import com.capstone.safeGuard.member.domain.infrastructure.MemberBatteryRepository;
import com.capstone.safeGuard.member.domain.infrastructure.MemberRepository;
import com.capstone.safeGuard.member.domain.infrastructure.ParentingRepository;
import com.capstone.safeGuard.notice.domain.domain.Confirm;
import com.capstone.safeGuard.notice.domain.domain.Emergency;
import com.capstone.safeGuard.notice.domain.domain.EmergencyReceiver;
import com.capstone.safeGuard.notice.domain.domain.Notice;
import com.capstone.safeGuard.notice.domain.infrastructure.ConfirmRepository;
import com.capstone.safeGuard.notice.domain.infrastructure.EmergencyReceiverRepository;
import com.capstone.safeGuard.notice.domain.infrastructure.EmergencyRepository;
import com.capstone.safeGuard.notice.domain.infrastructure.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
    private final BatteryService batteryService;
    private final MemberBatteryRepository memberBatteryRepository;
    private final ChildBatteryRepository childBatteryRepository;
    private final EmergencyRepository emergencyRepository;
    private final CoordinateRepository coordinateRepository;
    private final MemberFileRepository memberFileRepository;
    private final ChildFileRepository childFileRepository;
    private final EmergencyReceiverRepository emergencyReceiverRepository;
    private final NoticeRepository noticeRepository;

    @Transactional
    public Member memberLogin(LoginRequestDTO dto) {
        // 존재하는 멤버인가
        Optional<Member> foundMember = memberRepository.findById(dto.getEditTextID());
        if (foundMember.isEmpty()) {
            return null;
        }

        // ID와 PW가 일치하는가
        Member member = findMemberWithAuthenticate(foundMember.get(), dto.getEditTextPW());
        if (member == null) {
            return null;
        }

        String fcmToken = dto.getFcmToken();

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

    public Child childLogin(LoginRequestDTO dto) {
        Optional<Child> findChild = Optional.ofNullable(childRepository.findBychildName(dto.getEditTextID()));

        return findChild
                .map(child -> findChildWithAuthenticate(child, dto.getEditTextPW()))
                .orElse(null);

    }

    private Child findChildWithAuthenticate(Child findChild, String rawPassword) {
        if (passwordEncoder.matches(rawPassword, findChild.getChildPassword())) {
            return findChild;
        }
        return null;
    }

    public Boolean signup(SignUpRequestDTO dto) {
        Optional<Member> findMember = memberRepository.findById(dto.getInputID());
        if (findMember.isPresent()) {
            return false;
        }

        String email = dto.getInputEmail();
        if (checkEmailDuplicate(email)) {
            log.info("Email Duplicate");
            return false;
        }

        Member member = new Member();
        member.setMemberId(dto.getInputID());
        member.setEmail(dto.getInputEmail());
        member.setName(dto.getInputName());
        String encodedPassword = passwordEncoder.encode(dto.getInputPW());
        member.setPassword(encodedPassword);
        member.setAuthority(Authority.ROLE_MEMBER);
        member.setFcmToken(dto.getFcmToken());
        memberRepository.save(member);

        return true;
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public Boolean childSignUp(ChildSignUpRequestDTO childDto) {
        Optional<Child> findChild = Optional.ofNullable(childRepository.findBychildName(childDto.getChildName()));
        if (findChild.isPresent()) {
            return false;
        }
        log.info(childDto.getMemberId());
        log.info(childDto.getChildName());
        log.info(childDto.getChildPassword());

        Child child = new Child();
        child.setChildName(childDto.getChildName());
        String encodedPassword = passwordEncoder.encode(childDto.getChild_password());
        child.setChildPassword(encodedPassword);
        child.setAuthority(Authority.ROLE_CHILD);
        child.setLastStatus("일반구역");
        childRepository.save(child);

        // member child 연결
        String memberId = childDto.getMemberId();
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
    public Boolean addHelper(AddMemberDto addMemberDto) {
        Helping helping = new Helping();
        String childName = addMemberDto.getChildName();
        String memberId = addMemberDto.getParentId();

        Child selectedChild = childRepository.findBychildName(childName);
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
        Child selectedChild = childRepository.findBychildName(childName);
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
    public Boolean helperRemove(DrawHelperDTO dto) {
        Helping helping = helpingRepository.findByHelper_MemberIdAndChild_ChildName(dto.getMemberId(), dto.getChildName());
        if (helping == null) {
            return false;
        }

        ArrayList<Confirm> confirmList = confirmRepository.findAllByHelpingId(helping);
        confirmRepository.deleteAll(confirmList);

        helpingRepository.delete(helping);
        return true;
    }

    public boolean logout(String accessToken) {
        try {
            jwtService.toBlackList(accessToken);
        } catch (NoSuchElementException e) {
            return false;
        }
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

    public String findMemberId(FindMemberIdDTO dto) {
        Member foundMember = memberRepository.findByEmail(dto.getEmail());

        if (foundMember == null || (!foundMember.getName().equals(dto.getName()))) {
            return null;
        }

        return foundMember.getMemberId();
    }

    public String findChildNamesByParentId(String parentId) {
        Optional<Member> foundParent = memberRepository.findById(parentId);
        if (foundParent.isEmpty()) {
            return null;
        }

        List<Parenting> parentingList = foundParent.get().getParentingList();
        if (parentingList.isEmpty()) {
            return null;
        }

        return childNamesBuilder(parentingList);
    }

    private String childNamesBuilder(List<Parenting> parentingList) {
        StringBuilder childNames = new StringBuilder();
        int index = 0;

        childNames.append("{");
        for (Parenting parenting : parentingList) {
            childNames.append("\"ChildName")
                    .append(index + 1)
                    .append("\" : \"")
                    .append(parenting.getChild().getChildName())
                    .append("\"");

            if (index < parentingList.size() - 1) {
                childNames.append(",");
            }
        }
        childNames.append("}");

        return childNames.toString();
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
        if (foundCode.isPresent()) {
            emailAuthCodeRepository.delete(foundCode.get());
        }
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
    public boolean resetMemberPassword(ResetPasswordDTO dto) {
        Optional<Member> foundMember = memberRepository.findById(dto.getId());

        if (foundMember.isEmpty()) {
            return false;
        }

        foundMember.get().setPassword(passwordEncoder.encode(dto.getNewPassword()));
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
    public boolean resetChildPassword(ResetPasswordDTO dto) {
        Child foundChild = childRepository.findBychildName(dto.getId());

        if (foundChild == null) {
            return false;
        }

        foundChild.setChildPassword(passwordEncoder.encode(dto.getNewPassword()));
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
        Child foundChild = childRepository.findBychildName(id);
        if (foundChild == null) {
            return false;
        }

        foundChild.setLatitude(latitude);
        foundChild.setLongitude(longitude);

        return true;
    }

    @Transactional
    public Map<String, Double> getMemberCoordinate(String id) {
        Optional<Member> foundMember = memberRepository.findById(id);
        if (foundMember.isEmpty()) {
            return null;
        }

        Member member = foundMember.get();
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("latitude", member.getLatitude());
        coordinates.put("longitude", member.getLongitude());

        return coordinates;
    }

    @Transactional
    public Map<String, Double> getChildCoordinate(String id) {
        Child foundChild = childRepository.findBychildName(id);
        if (foundChild == null) {
            return null;
        }

        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("latitude", foundChild.getLatitude());
        coordinates.put("longitude", foundChild.getLongitude());

        return coordinates;
    }


    public boolean isPresent(String id, boolean flag) {
        if (flag) {
            return memberRepository.findById(id).isPresent();
        }

        return childRepository.findBychildName(id) != null;
    }

    @Transactional
    public Child findChildByChildName(String childName) {
        return childRepository.findBychildName(childName);
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
        return memberRepository.findById(memberId).orElse(null);
    }

    @Transactional
    public boolean addParent(String memberId, String childName) {
        Optional<Member> foundMember = memberRepository.findById(memberId);
        if (foundMember.isEmpty()) {
            return false;
        }

        Child foundChild = childRepository.findBychildName(childName);
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
    public boolean updateMemberName(UpdateMemberNameDTO dto) {
        Member foundMember = findMemberById(dto.getUserID());
        if (foundMember == null) {
            return false;
        }

        foundMember.setName(dto.getNickname());
        return true;
    }

    public String getNicknameById(String memberId) {
        Optional<Member> foundMember = memberRepository.findById(memberId);
        if (foundMember.isEmpty()) {
            return null;
        }
        return foundMember.get().getName();
    }
}