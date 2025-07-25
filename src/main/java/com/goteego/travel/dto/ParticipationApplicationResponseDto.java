package com.goteego.travel.dto;

import com.goteego.travel.domain.ParticipationApplication;
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
    private UserDto user;
    private String status;
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
    }
    
    /**
     * ParticipationApplication 엔티티를 DTO로 변환
     */
    public static ParticipationApplicationResponseDto from(ParticipationApplication application, User user) {
        return ParticipationApplicationResponseDto.builder()
                .applicationId(application.getId())
                .travelPostId(application.getTravelPostId())
                .user(UserDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImgUrl(user.getProfileImgUrl())
                        .build())
                .status(application.getStatus().name())
                .createdAt(application.getRequestedAt())
                .build();
    }
} 