package com.capstone.safeGuard.apis.file.presentation;

import com.capstone.safeGuard.apis.file.application.FileService;
import com.capstone.safeGuard.apis.file.presentation.request.FileUploadRequest;
import com.capstone.safeGuard.apis.file.presentation.request.GetFileRequest;
import com.capstone.safeGuard.apis.file.presentation.response.FilePersistResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {
	private final FileService fileService;

	@PostMapping("/upload-file")
	public ResponseEntity<FilePersistResponse> uploadFile(
		@RequestPart(value = "dto", required = false) FileUploadRequest dto,
		@RequestPart(value = "file") MultipartFile file) {
		String uploaderType = dto.uploaderType();
		String filePath;

		if (uploaderType.equalsIgnoreCase("Member")) {
			filePath = fileService.saveMemberFile(file, dto.uploaderId());
			if (filePath == null || filePath.isEmpty()) {
				return addErrorStatus();
			}
		} else if (uploaderType.equalsIgnoreCase("Child")) {
			filePath = fileService.saveChildFile(file, dto.uploaderId());
			if (filePath == null || filePath.isEmpty()) {
				return addErrorStatus();
			}
		} else {
			return addErrorStatus();
		}
		return addOkStatus(filePath);
	}

	@PostMapping("/get-file")
	public ResponseEntity<FilePersistResponse> getFile(@RequestBody GetFileRequest dto) {
		String userType = dto.userType();
		String filePath;

		if (userType.equalsIgnoreCase("Member")) {
			filePath = fileService.findMemberFile(dto.userId());
			if (filePath == null || filePath.isEmpty()) {
				return addErrorStatus();
			}
		} else if (userType.equalsIgnoreCase("Child")) {
			filePath = fileService.findChildFile(dto.userId());
			if (filePath == null || filePath.isEmpty()) {
				return addErrorStatus();
			}
		} else {
			return addErrorStatus();
		}

		return addOkStatus(filePath.substring(filePath.lastIndexOf("/") + 1));
	}

	private static ResponseEntity<FilePersistResponse> addOkStatus(String filePath) {
		return ResponseEntity
			.ok(FilePersistResponse.of(200, filePath));
	}

	private static ResponseEntity<FilePersistResponse> addErrorStatus() {
		return ResponseEntity
			.status(400)
			.body(FilePersistResponse.of(400, ""));
	}
}
