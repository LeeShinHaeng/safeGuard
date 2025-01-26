package com.capstone.safeGuard.apis.member.presentation;


import com.capstone.safeGuard.apis.member.application.ChildService;
import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.member.application.MemberUtil;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.MemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.response.ChildListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ChildNameListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ChildResponse;
import com.capstone.safeGuard.apis.member.presentation.response.HelpingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ParentingHelpingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ParentingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.StatusResponse;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RelationController {
	private final MemberService memberService;
	private final ChildService childService;
	private final MemberUtil memberUtil;

	@PostMapping("/add-helper")
	public ResponseEntity<String> addHelper(@RequestBody MemberRegisterRequest dto,
											BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}
		memberUtil.addHelper(dto);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/helper-remove")
	public ResponseEntity<String> helperRemove(@RequestBody HelperRemoveRequest dto,
											   BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		memberUtil.helperRemove(dto);
		return ResponseEntity.ok().build();
	}


	@PostMapping("/add-parent")
	public ResponseEntity<StatusResponse> addParent(@RequestBody MemberRegisterRequest dto) {
		Member foundMember = memberUtil.findMemberById(dto.parentId());
		Child foundChild = memberUtil.findChildByName(dto.childName());

		if (foundChild == null) {
			return ResponseEntity.ok(StatusResponse.of("400"));
		}

		memberUtil.addParent(foundMember.getMemberId(), foundChild.getChildName());
		return ResponseEntity.ok(StatusResponse.of("200"));
	}


	@PostMapping("/find-child-list")
	public ResponseEntity<ChildNameListResponse> findChildNameList(@RequestBody MemberIdRequest dto) {
		Map<String, String> childList = memberService.getChildList(dto.memberId());
		return ResponseEntity.ok(ChildNameListResponse.fromMap(childList));
	}

	@PostMapping("/find-parenting-helping-list")
	public ResponseEntity<ParentingHelpingListResponse> findParentingAndHelpingList(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			ParentingHelpingListResponse.from(
				memberService.getChildList(dto.memberId()),
				memberService.getHelpingList(dto.memberId())
			));
	}

	@PostMapping("/find-helping-list")
	public ResponseEntity<HelpingListResponse> findHelpingList(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			HelpingListResponse.from(
				memberService.getHelpingList(dto.memberId())
			)
		);
	}

	@PostMapping("/find-parenting-list")
	public ResponseEntity<ParentingListResponse> choseChildForm(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			ParentingListResponse.from(
				memberService.getChildList(dto.memberId())
			)
		);
	}

	//로그인한 멤버의 자식(그룹)들을 찾아서 반환
	@PostMapping("/group")
	public ResponseEntity<ChildListResponse> showChildList(@RequestBody MemberIdRequest dto) {
		String memberId = dto.memberId();

		log.info(memberId + "의 자식 리스트 반환 ");
		List<Child> childList = childService.getChildList(memberId);
		if (childList == null) {
			log.info("NULL");
			return ResponseEntity.ok(ChildListResponse.fromChildList(null));
		}

		List<ChildResponse> responses = new ArrayList<>();
		for (Child child : childList) {
			responses.add(ChildResponse.of(
				child.getId().toString(),
				child.getChildName(),
				child.getLastStatus()
			));
		}

		return ResponseEntity.ok(ChildListResponse.fromChildList(responses));
	}


	@PostMapping("/find-member-by-child")
	public ResponseEntity<ParentingHelpingListResponse> findMemberByChild(@RequestBody GetIdRequest dto) {
		Child foundChild = memberUtil.findChildByName(dto.id());
		if (foundChild == null) {
			return ResponseEntity.status(400).build();
		}

		Map<String, String> memberMap1 = new HashMap<>();
		List<Parenting> parentingList = foundChild.getParentingList();
		if (parentingList != null) {
			for (int i = 0; i < parentingList.size(); i++) {
				memberMap1.put(
					String.valueOf(i + 1),
					parentingList.get(i).getParent().getMemberId()
				);
			}
		}

		Map<String, String> memberMap2 = new HashMap<>();
		List<Helping> helpingList = foundChild.getHelpingList();
		if (helpingList != null) {
			for (int i = 0; i < helpingList.size(); i++) {
				memberMap2.put(
					String.valueOf(i + 1),
					helpingList.get(i).getHelper().getMemberId()
				);
			}
		}

		return ResponseEntity.ok().body(ParentingHelpingListResponse.from(memberMap1, memberMap2));
	}
}
