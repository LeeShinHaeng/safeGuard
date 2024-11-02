package com.capstone.safeGuard.file.apis.presentation;

import com.capstone.safeGuard.file.apis.presentation.request.FileUploadRequestDTO;
import com.capstone.safeGuard.file.apis.presentation.request.GetFileRequestDTO;
import com.capstone.safeGuard.file.apis.application.FileService;
import com.capstone.safeGuard.member.apis.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final MemberService memberService;
    private final FileService fileService;


    @PostMapping("/upload-file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestPart(value = "dto", required = false) FileUploadRequestDTO dto,
                                                          @RequestPart(value = "file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        String uploaderType = dto.getUploaderType();
        log.info("uploader type: " + uploaderType);

        String filePath;

        if (uploaderType.equalsIgnoreCase("Member")) {
            filePath = fileService.saveMemberFile(file, dto.getUploaderId());
            if (filePath == null || filePath.isEmpty()) {
                return addErrorStatus(result);
            }
        } else if (uploaderType.equalsIgnoreCase("Child")) {
            filePath = fileService.saveChildFile(file, dto.getUploaderId());
            if (filePath == null || filePath.isEmpty()) {
                return addErrorStatus(result);
            }
        } else {
            return addErrorStatus(result);
        }
        result.put("filePath", filePath);
        return addOkStatus(result);
    }

    @PostMapping("/get-file")
    public ResponseEntity<Map<String, String>> getFile(@RequestBody GetFileRequestDTO dto) {
        Map<String, String> result = new HashMap<>();

        String userType = dto.getUserType();
        String filePath;
        if (userType.equalsIgnoreCase("Member")) {
            filePath = fileService.findMemberFile(dto.getUserId());
            if (filePath == null || filePath.isEmpty()) {
                return addErrorStatus(result);
            }
        } else if (userType.equalsIgnoreCase("Child")) {
            filePath = fileService.findChildFile(dto.getUserId());
            if (filePath == null || filePath.isEmpty()) {
                return addErrorStatus(result);
            }
        } else {
            return addErrorStatus(result);
        }

        result.put("filePath", filePath.substring(filePath.lastIndexOf("/") + 1));
        return addOkStatus(result);
    }

    private static ResponseEntity<Map<String, String>> addOkStatus(Map<String, String> result) {
        result.put("status", "200");
        return ResponseEntity.ok().body(result);
    }

    private static ResponseEntity<Map<String, String>> addErrorStatus(Map<String, String> result) {
        result.put("status", "400");
        return ResponseEntity.status(400).body(result);
    }
}
