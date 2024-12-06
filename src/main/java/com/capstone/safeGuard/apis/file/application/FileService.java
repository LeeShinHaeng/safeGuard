package com.capstone.safeGuard.apis.file.application;

import com.capstone.safeGuard.domain.file.domain.ChildFile;
import com.capstone.safeGuard.domain.file.domain.MemberFile;
import com.capstone.safeGuard.domain.file.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.domain.file.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
	public static final String SAVE_PATH = "/root/capstone/photos/";
	private final MemberFileRepository memberFileRepository;
	private final MemberRepository memberRepository;
	private final ChildRepository childRepository;
	private final ChildFileRepository childFileRepository;

	@Transactional
	public String saveMemberFile(MultipartFile file, String memberId) {
		Member foundMember = memberRepository
			.findById(memberId)
			.orElseThrow(() -> new RuntimeException("No Such Member"));

		presentCheckMember(foundMember);

		String fileName = makeFileName(file.getOriginalFilename());
		String filePath = SAVE_PATH + fileName;

		try {
			file.transferTo(new File(filePath));
			memberFileRepository.save(
				MemberFile.builder()
					.fileName(filePath)
					.member(foundMember)
					.build()
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fileName;
	}

	private String makeFileName(String originalFilename) {
		UUID uuid = UUID.randomUUID();
		return uuid + "-" + originalFilename;
	}

	private void presentCheckMember(Member foundMember) {
		memberFileRepository
			.findByMember(foundMember)
			.ifPresent(memberFileRepository::delete);
	}

	@Transactional
	public String saveChildFile(MultipartFile file, String childName) {
		Child foundChild = childRepository.findByChildName(childName)
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		presentCheckChild(foundChild);

		String fileName = makeFileName(file.getOriginalFilename());
		String filePath = SAVE_PATH + fileName;

		try {
			file.transferTo(new File(filePath));
			childFileRepository.save(
				ChildFile.builder()
					.fileName(filePath)
					.child(foundChild)
					.build()
			);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fileName;
	}

	private void presentCheckChild(Child foundChild) {
		Optional<ChildFile> foundChildFile = childFileRepository.findByChild(foundChild);
		foundChildFile.ifPresent(childFileRepository::delete);
	}

	@Transactional
	public String findMemberFile(String userId) {
		Member foundMember = memberRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		Optional<MemberFile> foundFile = memberFileRepository.findByMember(foundMember);
		return foundFile.map(MemberFile::getFileName)
			.orElseThrow(() -> new RuntimeException("No Such File"));
	}

	@Transactional
	public String findChildFile(String userId) {
		Child foundChild = childRepository.findByChildName(userId)
			.orElseThrow(() -> new RuntimeException("No Such Child"));

		Optional<ChildFile> foundFile = childFileRepository.findByChild(foundChild);
		return foundFile.map(ChildFile::getFileName)
			.orElseThrow(() -> new RuntimeException("No Such File"));
	}
}
