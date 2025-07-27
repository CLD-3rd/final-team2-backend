package com.goteego.profileAnswer.dto;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 취향 설문 응답 DTO 클래스
 * 클라이언트에게 여행 취향 설문 정보를 전달할 때 사용되는 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Builder
public class ProfileAnswerResponseDto {
    
    /**
     * 설문 고유 식별자
     */
    private Long answerId;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 여행 중 성향 목록
     */
    private List<String> travelTendencies;
    
    /**
     * 선호하는 활동 목록
     */
    private List<String> preferredActivities;
    
    /**
     * 여행 일정 스타일
     */
    private String scheduleStyle;
    
    /**
     * 여행 중 술자리에 대한 의견
     */
    private String drinkingPreference;
    
    /**
     * 흡연 여부
     */
    private Boolean smokingStatus;
    
    /**
     * 선호하는 여행지 유형 목록
     */
    private List<String> preferredDestinations;
    
    /**
     * 설문 완료 여부
     */
    private Boolean isCompleted;
    
    /**
     * 설문 생성 날짜
     */
    private LocalDate createdAt;
    
    /**
     * 설문 수정 날짜
     */
    private LocalDate modifiedAt;
    
    /**
     * ProfileAnswer 엔티티를 ProfileAnswerResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param profileAnswer 변환할 ProfileAnswer 엔티티
     * @return 변환된 ProfileAnswerResponseDto 객체
     */
    public static ProfileAnswerResponseDto from(ProfileAnswer profileAnswer) {
        return ProfileAnswerResponseDto.builder()
                .answerId(profileAnswer.getId())
                .userId(profileAnswer.getUserId())
                .travelTendencies(parseJsonToList(profileAnswer.getTravelTendencies()))
                .preferredActivities(parseJsonToList(profileAnswer.getPreferredActivities()))
                .scheduleStyle(profileAnswer.getScheduleStyle())
                .drinkingPreference(profileAnswer.getDrinkingPreference())
                .smokingStatus(profileAnswer.getSmokingStatus())
                .preferredDestinations(parseJsonToList(profileAnswer.getPreferredDestinations()))
                .isCompleted(profileAnswer.getIsCompleted())
                .createdAt(profileAnswer.getCreatedAt().toLocalDate())
                .modifiedAt(profileAnswer.getModifiedAt().toLocalDate())
                .build();
    }
    
    /**
     * JSON 문자열을 List로 파싱하는 헬퍼 메서드
     * 실제 구현에서는 Jackson ObjectMapper 등을 사용할 수 있음
     * 
     * @param jsonString JSON 문자열
     * @return 파싱된 문자열 리스트
     */
    private static List<String> parseJsonToList(String jsonString) {
        // TODO: 실제 JSON 파싱 로직 구현
        // 현재는 간단한 구현을 위해 null 체크만 수행
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return List.of();
        }
        // 실제로는 ObjectMapper를 사용하여 JSON을 파싱해야 함
        return List.of(jsonString.split(","));
    }
} 