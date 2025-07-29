package com.goteego.profileAnswer.domain;

import com.goteego.global.domain.BaseEntity;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
// BaseEntity에서 상속받으므로 별도 import 불필요

import java.time.LocalDateTime;

/**
 * 사용자 선호도 엔티티 클래스
 * 사용자의 여행 성향과 선호도를 저장하는 엔티티 (user_prefer 테이블)
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_prefer")
public class ProfileAnswer extends BaseEntity {
    
    /**
     * 기본키 (사용자 ID와 동일)
     */
    @Id
    @Column(name = "user_id")
    private Long id;
    
    /**
     * 사용자
     * User 엔티티와 OneToOne 관계
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    // 🍶 술 관련 선호도
    @Column(name = "is_alchol3")
    private Boolean isAlchol3; // 술 좋아해요
    
    @Column(name = "is_alchol2")
    private Boolean isAlchol2; // 분위기상 한두 잔 정도
    
    @Column(name = "is_alchol1")
    private Boolean isAlchol1; // 술은 즐기지 않아요
    
    @Column(name = "is_smoker")
    private Boolean isSmoker; // 흡연 유무
    
    // 🤝 성격 관련 선호도
    @Column(name = "is_friendly")
    private Boolean isFriendly; // 새로운 사람과도 금방 친해져요
    
    @Column(name = "is_quiet")
    private Boolean isQuiet; // 조용한 분위기를 좋아해요
    
    @Column(name = "is_lead")
    private Boolean isLead; // 앞장서서 리드하는 편이에요
    
    @Column(name = "is_party")
    private Boolean isParty; // 분위기를 띄우는 걸 좋아해요
    
    @Column(name = "is_search")
    private Boolean isSearch; // 여행 중에도 정보를 꼼꼼히 찾는 편이에요
    
    @Column(name = "is_listen")
    private Boolean isListen; // 다른 사람 의견을 잘 들어주는 편이에요
    
    // 🏞 활동 관련 선호도
    @Column(name = "is_see")
    private Boolean isSee; // 자연 경관 감상
    
    @Column(name = "is_cafe")
    private Boolean isCafe; // 카페/휴식
    
    @Column(name = "is_taste")
    private Boolean isTaste; // 맛집 탐방
    
    @Column(name = "is_picture")
    private Boolean isPicture; // 사진 촬영
    
    @Column(name = "is_shopping")
    private Boolean isShopping; // 쇼핑
    
    @Column(name = "is_outdoor")
    private Boolean isOutdoor; // 액티비티(서핑 등산 등)
    
    // 💤 여행 스타일 관련 선호도
    @Column(name = "is_chill")
    private Boolean isChill; // 느긋하게 여유롭게
    
    @Column(name = "is_busy")
    private Boolean isBusy; // 빡빡하고 알차게
    
    @Column(name = "is_flex")
    private Boolean isFlex; // 상황에 따라 유동적으로
    
    // 🌆 여행지 유형 관련 선호도
    @Column(name = "is_city")
    private Boolean isCity; // 도시/핫플 위주
    
    @Column(name = "is_heal")
    private Boolean isHeal; // 자연/힐링 위주
    
    @Column(name = "is_beach")
    private Boolean isBeach; // 바다/해변
    
    @Column(name = "is_mountain")
    private Boolean isMountain; // 산/등산
    
    // BaseEntity에서 상속받은 createdAt, lastModifiedAt 사용
    // 별도로 정의하지 않음
    
    /**
     * 사용자 선호도 생성 빌더 메서드
     */
    @Builder
    public ProfileAnswer(User user, Boolean isAlchol3, Boolean isAlchol2, Boolean isAlchol1, 
                        Boolean isSmoker, Boolean isFriendly, Boolean isQuiet, Boolean isLead, 
                        Boolean isParty, Boolean isSearch, Boolean isListen, Boolean isSee, 
                        Boolean isCafe, Boolean isTaste, Boolean isPicture, Boolean isShopping, 
                        Boolean isOutdoor, Boolean isChill, Boolean isBusy, Boolean isFlex, 
                        Boolean isCity, Boolean isHeal, Boolean isBeach, Boolean isMountain) {
        this.user = user;
        this.isAlchol3 = isAlchol3;
        this.isAlchol2 = isAlchol2;
        this.isAlchol1 = isAlchol1;
        this.isSmoker = isSmoker;
        this.isFriendly = isFriendly;
        this.isQuiet = isQuiet;
        this.isLead = isLead;
        this.isParty = isParty;
        this.isSearch = isSearch;
        this.isListen = isListen;
        this.isSee = isSee;
        this.isCafe = isCafe;
        this.isTaste = isTaste;
        this.isPicture = isPicture;
        this.isShopping = isShopping;
        this.isOutdoor = isOutdoor;
        this.isChill = isChill;
        this.isBusy = isBusy;
        this.isFlex = isFlex;
        this.isCity = isCity;
        this.isHeal = isHeal;
        this.isBeach = isBeach;
        this.isMountain = isMountain;
    }
    
    /**
     * 사용자 선호도 정보를 업데이트하는 메서드
     */
    public void update(Boolean isAlchol3, Boolean isAlchol2, Boolean isAlchol1, 
                      Boolean isSmoker, Boolean isFriendly, Boolean isQuiet, Boolean isLead, 
                      Boolean isParty, Boolean isSearch, Boolean isListen, Boolean isSee, 
                      Boolean isCafe, Boolean isTaste, Boolean isPicture, Boolean isShopping, 
                      Boolean isOutdoor, Boolean isChill, Boolean isBusy, Boolean isFlex, 
                      Boolean isCity, Boolean isHeal, Boolean isBeach, Boolean isMountain) {
        this.isAlchol3 = isAlchol3;
        this.isAlchol2 = isAlchol2;
        this.isAlchol1 = isAlchol1;
        this.isSmoker = isSmoker;
        this.isFriendly = isFriendly;
        this.isQuiet = isQuiet;
        this.isLead = isLead;
        this.isParty = isParty;
        this.isSearch = isSearch;
        this.isListen = isListen;
        this.isSee = isSee;
        this.isCafe = isCafe;
        this.isTaste = isTaste;
        this.isPicture = isPicture;
        this.isShopping = isShopping;
        this.isOutdoor = isOutdoor;
        this.isChill = isChill;
        this.isBusy = isBusy;
        this.isFlex = isFlex;
        this.isCity = isCity;
        this.isHeal = isHeal;
        this.isBeach = isBeach;
        this.isMountain = isMountain;
        // BaseEntity의 JPA Auditing이 자동으로 처리
    }
    
    /**
     * 작성자 확인 메서드
     * 
     * @param userId 확인할 사용자 ID
     * @return 작성자 여부
     */
    public boolean isAuthor(Long userId) {
        return this.user.getId().equals(userId);
    }
} 