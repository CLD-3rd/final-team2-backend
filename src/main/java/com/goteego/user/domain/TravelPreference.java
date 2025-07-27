package com.goteego.user.domain;

import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 여행 취향 설문 엔티티 클래스
 * 사용자의 여행 성향과 선호도를 저장하는 엔티티
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_preferences")
public class TravelPreference extends BaseEntity {
    
    /**
     * 여행 취향 고유 식별자 (Primary Key)
     * 자동 증가하는 ID 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;
    
    /**
     * 여행 취향을 가진 사용자 ID
     * 외래키로 사용되며, User 엔티티와 연결됨
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 여행 중 성향 (JSON 형태로 저장)
     * 예: ["새로운 사람과도 금방 친해져요", "조용한 분위기를 좋아해요"]
     */
    @Column(name = "travel_tendencies", columnDefinition = "TEXT")
    private String travelTendencies;
    
    /**
     * 선호하는 활동들 (JSON 형태로 저장)
     * 예: ["자연 경관 감상", "카페/휴식", "맛집 탐방"]
     */
    @Column(name = "preferred_activities", columnDefinition = "TEXT")
    private String preferredActivities;
    
    /**
     * 여행 일정 스타일
     * "느긋하게 여유롭게", "빡빡하고 알차게", "상황에 따라 유동적으로"
     */
    @Column(name = "schedule_style", length = 50)
    private String scheduleStyle;
    
    /**
     * 여행 중 술자리에 대한 의견
     * "술 좋아해요", "분위기상 한두 잔 정도", "술은 즐기지 않아요"
     */
    @Column(name = "drinking_preference", length = 50)
    private String drinkingPreference;
    
    /**
     * 흡연 여부
     */
    @Column(name = "smoking_status")
    private Boolean smokingStatus;
    
    /**
     * 선호하는 여행지 유형 (JSON 형태로 저장)
     * 예: ["어디든 좋아요!", "도시/핫플 위주", "자연/힐링 위주"]
     */
    @Column(name = "preferred_destinations", columnDefinition = "TEXT")
    private String preferredDestinations;
    
    /**
     * 설문 완료 여부
     */
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    /**
     * 설문 생성 시간
     * JPA Auditing을 통해 자동으로 설정됨
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 설문 수정 시간
     * JPA Auditing을 통해 자동으로 업데이트됨
     */
    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    /**
     * 여행 취향 설문 생성 빌더 메서드
     * 
     * @param userId 사용자 ID
     * @param travelTendencies 여행 중 성향
     * @param preferredActivities 선호하는 활동들
     * @param scheduleStyle 여행 일정 스타일
     * @param drinkingPreference 술자리 의견
     * @param smokingStatus 흡연 여부
     * @param preferredDestinations 선호하는 여행지 유형
     */
    @Builder
    public TravelPreference(Long userId, String travelTendencies, String preferredActivities, 
                           String scheduleStyle, String drinkingPreference, Boolean smokingStatus, 
                           String preferredDestinations) {
        this.userId = userId;
        this.travelTendencies = travelTendencies;
        this.preferredActivities = preferredActivities;
        this.scheduleStyle = scheduleStyle;
        this.drinkingPreference = drinkingPreference;
        this.smokingStatus = smokingStatus;
        this.preferredDestinations = preferredDestinations;
        this.isCompleted = true;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 여행 취향 정보를 업데이트하는 메서드
     * 
     * @param travelTendencies 여행 중 성향
     * @param preferredActivities 선호하는 활동들
     * @param scheduleStyle 여행 일정 스타일
     * @param drinkingPreference 술자리 의견
     * @param smokingStatus 흡연 여부
     * @param preferredDestinations 선호하는 여행지 유형
     */
    public void update(String travelTendencies, String preferredActivities, String scheduleStyle,
                      String drinkingPreference, Boolean smokingStatus, String preferredDestinations) {
        this.travelTendencies = travelTendencies;
        this.preferredActivities = preferredActivities;
        this.scheduleStyle = scheduleStyle;
        this.drinkingPreference = drinkingPreference;
        this.smokingStatus = smokingStatus;
        this.preferredDestinations = preferredDestinations;
        this.isCompleted = true;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 설문 완료 여부를 설정하는 메서드
     * 
     * @param isCompleted 완료 여부
     */
    public void setCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        this.modifiedAt = LocalDateTime.now();
    }
} 