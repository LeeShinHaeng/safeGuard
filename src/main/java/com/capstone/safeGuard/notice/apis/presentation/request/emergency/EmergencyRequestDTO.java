package com.capstone.safeGuard.notice.apis.presentation.request.emergency;

import com.capstone.safeGuard.member.domain.domain.Child;
import com.capstone.safeGuard.notice.domain.domain.Emergency;
import com.capstone.safeGuard.member.domain.domain.Member;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmergencyRequestDTO {
    private String senderId;
    private String childName;
    private double latitude;
    private double longitude;

    private final String title = "주변 피보호자에게 도움이 필요합니다.";

    public Emergency dtoToDomain(Member member, Child child, String content){
        return Emergency
                .builder()
                .title(title)
                .content(content)
                .senderId(member)
                .child(child)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
