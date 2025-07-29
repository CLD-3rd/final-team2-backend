package com.goteego.profileAnswer.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 선호도 요청 DTO 클래스
 * 클라이언트로부터 사용자 선호도 데이터를 받기 위한 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Setter
public class ProfileAnswerRequestDto {
    
    // 🍶 술 관련 선호도
    private Boolean isAlchol3; // 🍶 술 좋아해요
    private Boolean isAlchol2; // 🍻 분위기상 한두 잔 정도
    private Boolean isAlchol1; // 🚫 술은 즐기지 않아요
    private Boolean isSmoker;  // 흡연 유무
    
    // 🤝 성격 관련 선호도
    private Boolean isFriendly; // 🤝 새로운 사람과도 금방 친해져요
    private Boolean isQuiet;    // 🤫 조용한 분위기를 좋아해요
    private Boolean isLead;     // 🧭 앞장서서 리드하는 편이에요
    private Boolean isParty;    // 😎 분위기를 띄우는 걸 좋아해요
    private Boolean isSearch;   // 🤓 여행 중에도 정보를 꼼꼼히 찾는 편이에요
    private Boolean isListen;   // 👂 다른 사람 의견을 잘 들어주는 편이에요
    
    // 🏞 활동 관련 선호도
    private Boolean isSee;      // 🏞 자연 경관 감상
    private Boolean isCafe;     // 🧘 카페/휴식
    private Boolean isTaste;    // 🍽 맛집 탐방
    private Boolean isPicture;  // 📸 사진 촬영
    private Boolean isShopping; // 🛍 쇼핑
    private Boolean isOutdoor;  // 🏃 액티비티(서핑 등산 등)
    
    // 💤 여행 스타일 관련 선호도
    private Boolean isChill;    // 💤 느긋하게 여유롭게
    private Boolean isBusy;     // 🕘 빡빡하고 알차게
    private Boolean isFlex;     // ❔ 상황에 따라 유동적으로
    
    // 🌆 여행지 유형 관련 선호도
    private Boolean isCity;     // 🌆 도시/핫플 위주
    private Boolean isHeal;     // 🏞 자연/힐링 위주
    private Boolean isBeach;    // 🏖 바다/해변
    private Boolean isMountain; // 🗻 산/등산
} 