package com.capstone.safeGuard.file.apis.application;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.file.domain.domain.ChildFile;
import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.file.domain.domain.MemberFile;
import com.capstone.safeGuard.file.domain.infrastructure.ChildFileRepository;
import com.capstone.safeGuard.member.domain.infrastructure.ChildRepository;
import com.capstone.safeGuard.file.domain.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.member.domain.infrastructure.MemberRepository;
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
    public static final String SAVEPATH = "/root/capstone/photos/";
    private final MemberFileRepository memberFileRepository;
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final ChildFileRepository childFileRepository;

    @Transactional
    public String saveMemberFile(MultipartFile file, String memberId) {
        String originalFilename = file.getOriginalFilename();
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "-" + originalFilename;
        String filePath = SAVEPATH + fileName;

        Optional<Member> foundMember = memberRepository.findById(memberId);
        if (foundMember.isEmpty()) {
            return null;
        }

        Optional<MemberFile> foundMemberFile = memberFileRepository.findByMember(foundMember.get());
        foundMemberFile.ifPresent(memberFileRepository::delete);

        try {
            file.transferTo(new File(filePath));
            memberFileRepository.save(
                    MemberFile.builder()
                            .fileName(filePath)
                            .member(foundMember.get())
                            .build()
            );

        } catch (IOException e) {
            return null;
        }
        return fileName;
    }

    @Transactional
    public String saveChildFile(MultipartFile file, String childName) {
        String originalFilename = file.getOriginalFilename();
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "-" + originalFilename;
        String filePath = SAVEPATH + fileName;

        Child foundChild = childRepository.findByChildName(childName);
        if (foundChild == null) {
            return null;
        }

        Optional<ChildFile> foundChildFile = childFileRepository.findByChild(foundChild);
        foundChildFile.ifPresent(childFileRepository::delete);

        try {
            file.transferTo(new File(filePath));
            childFileRepository.save(
                    ChildFile.builder()
                            .fileName(filePath)
                            .child(foundChild)
                            .build()
            );

        } catch (IOException e) {
            return null;
        }
        return fileName;
    }

    @Transactional
    public String findMemberFile(String userId) {
        Optional<Member> foundMember = memberRepository.findById(userId);
        if (foundMember.isEmpty()) {
            return null;
        }

        Optional<MemberFile> foundFile = memberFileRepository.findByMember(foundMember.get());
        return foundFile.map(MemberFile::getFileName).orElse(null);
    }

    @Transactional
    public String findChildFile(String userId) {
        Child foundChild = childRepository.findByChildName(userId);
        if (foundChild == null) {
            return null;
        }

        Optional<ChildFile> foundFile = childFileRepository.findByChild(foundChild);
        return foundFile.map(ChildFile::getFileName).orElse(null);
    }
}
