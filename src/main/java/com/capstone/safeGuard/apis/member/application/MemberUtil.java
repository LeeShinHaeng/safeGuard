package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.HelpingRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.capstone.safeGuard.domain.member.infrastructure.ParentingRepository;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import com.capstone.safeGuard.domain.notice.domain.EmergencyReceiver;
import com.capstone.safeGuard.domain.notice.infrastructure.ConfirmRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyReceiverRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberUtil {
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;
	private final ParentingRepository parentingRepository;
	private final HelpingRepository helpingRepository;

	private final ConfirmRepository confirmRepository;
	private final EmergencyRepository emergencyRepository;
	private final EmergencyReceiverRepository emergencyReceiverRepository;

	@Transactional
	public Child findChildByName(String name) {
		return childRepository.findByChildName(name)
			.orElseThrow(() -> new RuntimeException("Child not found"));
	}

	@Transactional
	public Member findMemberById(String memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new RuntimeException("Member not found"));
	}

	@Transactional
	public void saveParenting(Member parent, Child child) {
		Parenting parenting = new Parenting();
		parenting.setParent(parent);
		parenting.setChild(child);

		parent.getParentingList().add(parenting);
		child.getParentingList().add(parenting);

		parentingRepository.save(parenting);
	}

	@Transactional
	public void addHelper(MemberRegisterRequest memberRegisterRequest) {
		Helping helping = new Helping();
		String childName = memberRegisterRequest.childName();
		String memberId = memberRegisterRequest.parentId();

		Child selectedChild = findChildByName(childName);
		Member findMember = findMemberById(memberId);

		List<Helping> foundHelpingList = helpingRepository.findAllByHelper(findMember);
		for (Helping foundHelping : foundHelpingList) {
			if (foundHelping.getChild().equals(selectedChild)) {
				return;
			}
		}

		helping.setHelper(findMember);
		helping.setChild(selectedChild);

		helpingRepository.save(helping);
	}


	@Transactional
	public void deleteParentingList(List<Parenting> parentingList) {
		if (parentingList != null) {
			parentingRepository.deleteAll(parentingList);
		}
	}

	@Transactional
	public void deleteHelpingList(List<Helping> helpingList) {
		if (helpingList != null) {
			for (Helping helping : helpingList) {
				ArrayList<Confirm> confirmArrayList = confirmRepository.findAllByHelpingId(helping);
				if (!confirmArrayList.isEmpty()) {
					confirmRepository.deleteAll(confirmArrayList);
				}
			}
			helpingRepository.deleteAll(helpingList);
		}
	}

	@Transactional
	public void deleteEmergencyList(List<Emergency> emergencyList) {
		if (emergencyList != null) {
			for (Emergency emergency : emergencyList) {
				List<EmergencyReceiver> emergencyReceiverList = emergencyReceiverRepository.findAllByEmergency(emergency);
				if (!emergencyReceiverList.isEmpty()) {
					emergencyReceiverRepository.deleteAll(emergencyReceiverList);
				}
			}
			emergencyRepository.deleteAll(emergencyList);
		}
	}

	@Transactional
	public void helperRemove(HelperRemoveRequest dto) {
		Helping helping = helpingRepository.findByHelperMemberIdAndChildChildName(dto.memberId(), dto.childName())
			.orElseThrow(() -> new IllegalStateException("helping Not Found"));
		ArrayList<Confirm> confirmList = confirmRepository.findAllByHelpingId(helping);

		confirmRepository.deleteAll(confirmList);
		helpingRepository.delete(helping);
	}

	@Transactional
	public Child findChildByChildName(String childName) {
		return childRepository.findByChildName(childName)
			.orElse(null);
	}

	@Transactional
	public void addParent(String memberId, String childName) {
		Member foundMember = findMemberById(memberId);

		Child foundChild = findChildByChildName(childName);

		List<Parenting> foundParentingList = parentingRepository.findAllByParent(foundMember);
		for (Parenting foundParenting : foundParentingList) {
			if (foundParenting.getChild().equals(foundChild)) {
				return;
			}
		}

		saveParenting(foundMember, foundChild);
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

	public boolean isPresent(String id, boolean flag) {
		if (flag) {
			return memberRepository.findById(id).isPresent();
		}
		return childRepository.findByChildName(id).isPresent();
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
}
