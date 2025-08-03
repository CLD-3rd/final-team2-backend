package com.goteego.travelPost.dto.participation;

import com.goteego.travelPost.domain.ParticipationApplication;
import com.goteego.user.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 참가 신청 응답 DTO
 * API 응답 시 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationApplicationResponseDto {

    private Long applicationId;
    private Long travelPostId;
    private Long participantId;
    private String participantNickname;
    private String profileImgUrl;
    private String status;
    private LocalDateTime createdAt;

    /**
     * ParticipationApplication 엔티티를 DTO로 변환
     */
    public static ParticipationApplicationResponseDto from(ParticipationApplication application, User user) {
        return ParticipationApplicationResponseDto.builder()
                .applicationId(application.getId())
                .travelPostId(application.getTravelPost().getId())
                .participantId(user.getId())
                .participantNickname(user.getNickname())
                .status(application.getStatus().name())
                .createdAt(application.getRequestedAt())
                .build();
    }
} 