package com.goteego.profileAnswer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 여행 취향 설문 요청 DTO 클래스
 * 클라이언트로부터 여행 취향 설문 데이터를 받기 위한 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Setter
public class ProfileAnswerRequestDto {
    
    /**
     * 여행 중 성향 목록
     * 예: ["새로운 사람과도 금방 친해져요", "조용한 분위기를 좋아해요"]
     */
    private List<String> travelTendencies;
    
    /**
     * 선호하는 활동 목록
     * 예: ["자연 경관 감상", "카페/휴식", "맛집 탐방"]
     */
    private List<String> preferredActivities;
    
    /**
     * 여행 일정 스타일
     * "느긋하게 여유롭게", "빡빡하고 알차게", "상황에 따라 유동적으로"
     */
    private String scheduleStyle;
    
    /**
     * 여행 중 술자리에 대한 의견
     * "술 좋아해요", "분위기상 한두 잔 정도", "술은 즐기지 않아요"
     */
    private String drinkingPreference;
    
    /**
     * 흡연 여부
     */
    private Boolean smokingStatus;
    
    /**
     * 선호하는 여행지 유형 목록
     * 예: ["어디든 좋아요!", "도시/핫플 위주", "자연/힐링 위주"]
     */
    private List<String> preferredDestinations;
} 