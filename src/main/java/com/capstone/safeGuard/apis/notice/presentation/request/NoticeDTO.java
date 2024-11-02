package com.capstone.safeGuard.apis.notice.presentation.request;

import com.capstone.safeGuard.domain.notice.domain.NoticeLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long noticeId;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotBlank
    private NoticeLevel noticeLevel;

    private LocalDateTime createdAt;
}
