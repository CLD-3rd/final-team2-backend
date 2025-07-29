package com.goteego.profileAnswer.service;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import com.goteego.profileAnswer.dto.ProfileAnswerRequestDto;
import com.goteego.profileAnswer.dto.ProfileAnswerResponseDto;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.profileAnswer.repository.ProfileAnswerRepository;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.recommendation.service.RecommendationService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 선호도 서비스 클래스
 * 사용자 선호도 관련 비즈니스 로직을 처리하는 서비스 계층
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileAnswerService {
    
    /**
     * 사용자 선호도 데이터 접근을 위한 리포지토리
     */
    private final ProfileAnswerRepository profileAnswerRepository;
    
    /**
     * 추천 시스템 서비스 (임베딩 생성용)
     */
    private final RecommendationService recommendationService;
    
    /**
     * 사용자의 선호도를 조회하는 메서드
     * 
     * @param user 조회할 사용자
     * @return 해당 사용자의 선호도 응답 DTO
     * @throws IllegalArgumentException 선호도를 찾을 수 없는 경우
     */
    public ProfileAnswerResponseDto getProfileAnswer(User user) {
        log.info("=== 사용자 선호도 조회 시작 ===");
        log.info("조회 요청 사용자 ID: {}", user.getId());
        
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("사용자 선호도를 찾을 수 없음 - userId: {}", user.getId());
                    return new BusinessException(ErrorCode.PROFILE_ANSWER_NOT_FOUND);
                });
        
        log.info("사용자 선호도 조회 완료 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        return ProfileAnswerResponseDto.from(profileAnswer);
    }
    
    /**
     * 사용자의 선호도 존재 여부를 확인하는 메서드
     * 
     * @param user 확인할 사용자
     * @return 선호도 존재 여부
     */
    public boolean existsByUser(User user) {
        return profileAnswerRepository.existsByUser(user);
    }
    
    /**
     * 새로운 사용자 선호도를 생성하는 메서드
     * 
     * @param user 사용자 객체
     * @param requestDto 선호도 요청 데이터
     * @return 생성된 선호도 엔티티
     */
    @Transactional
    public ProfileAnswer createProfileAnswer(User user, ProfileAnswerRequestDto requestDto) {
        log.info("=== 사용자 선호도 생성 시작 ===");
        log.info("생성 요청 사용자 ID: {}", user.getId());
        
        // ===== 내부로직: 사용자 선호도 중복 검증 및 엔티티 생성 =====
        if (profileAnswerRepository.existsByUser(user)) {
            log.warn("이미 사용자 선호도가 존재함 - userId: {}", user.getId());
            throw new BusinessException(ErrorCode.PROFILE_ANSWER_ALREADY_EXISTS);
        }
        
        ProfileAnswer profileAnswer = ProfileAnswer.builder()
                .user(user)
                .isAlchol3(requestDto.getIsAlchol3())
                .isAlchol2(requestDto.getIsAlchol2())
                .isAlchol1(requestDto.getIsAlchol1())
                .isSmoker(requestDto.getIsSmoker())
                .isFriendly(requestDto.getIsFriendly())
                .isQuiet(requestDto.getIsQuiet())
                .isLead(requestDto.getIsLead())
                .isParty(requestDto.getIsParty())
                .isSearch(requestDto.getIsSearch())
                .isListen(requestDto.getIsListen())
                .isSee(requestDto.getIsSee())
                .isCafe(requestDto.getIsCafe())
                .isTaste(requestDto.getIsTaste())
                .isPicture(requestDto.getIsPicture())
                .isShopping(requestDto.getIsShopping())
                .isOutdoor(requestDto.getIsOutdoor())
                .isChill(requestDto.getIsChill())
                .isBusy(requestDto.getIsBusy())
                .isFlex(requestDto.getIsFlex())
                .isCity(requestDto.getIsCity())
                .isHeal(requestDto.getIsHeal())
                .isBeach(requestDto.getIsBeach())
                .isMountain(requestDto.getIsMountain())
                .build();
        
        try {
            ProfileAnswer savedProfileAnswer = profileAnswerRepository.save(profileAnswer);
            log.info("사용자 선호도 저장 완료 - userId: {}, answerId: {}", user.getId(), savedProfileAnswer.getId());
            
            // 임베딩 생성은 별도로 처리 (실패해도 ProfileAnswer는 저장됨)
            try {
                createUserEmbeddingFromProfileAnswer(savedProfileAnswer);
            } catch (Exception embeddingError) {
                log.warn("임베딩 생성 실패했지만 ProfileAnswer는 저장됨 - userId: {}, error: {}", 
                        user.getId(), embeddingError.getMessage());
                // 임베딩 생성 실패는 ProfileAnswer 저장을 막지 않음
            }
            
            return savedProfileAnswer;
        } catch (Exception e) {
            log.error("사용자 선호도 생성 중 오류 발생 - userId: {}, error: {}", user.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.PROFILE_ANSWER_CREATION_FAILED);
        }
    }
    
    /**
     * 사용자 선호도를 수정하는 메서드
     * 
     * @param user 사용자 객체
     * @param requestDto 수정할 선호도 데이터
     * @return 수정된 선호도 엔티티
     * @throws IllegalArgumentException 선호도를 찾을 수 없는 경우
     */
    @Transactional
    public ProfileAnswer updateProfileAnswer(User user, ProfileAnswerRequestDto requestDto) {
        log.info("=== 사용자 선호도 수정 시작 ===");
        log.info("수정 요청 사용자 ID: {}", user.getId());
        
        // ===== 내부로직: 기존 선호도 조회 및 업데이트 =====
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("수정할 사용자 선호도를 찾을 수 없음 - userId: {}", user.getId());
                    return new BusinessException(ErrorCode.PROFILE_ANSWER_NOT_FOUND);
                });
        
        profileAnswer.update(
                requestDto.getIsAlchol3(), requestDto.getIsAlchol2(), requestDto.getIsAlchol1(),
                requestDto.getIsSmoker(), requestDto.getIsFriendly(), requestDto.getIsQuiet(),
                requestDto.getIsLead(), requestDto.getIsParty(), requestDto.getIsSearch(),
                requestDto.getIsListen(), requestDto.getIsSee(), requestDto.getIsCafe(),
                requestDto.getIsTaste(), requestDto.getIsPicture(), requestDto.getIsShopping(),
                requestDto.getIsOutdoor(), requestDto.getIsChill(), requestDto.getIsBusy(),
                requestDto.getIsFlex(), requestDto.getIsCity(), requestDto.getIsHeal(),
                requestDto.getIsBeach(), requestDto.getIsMountain()
        );
        
        try {
            createUserEmbeddingFromProfileAnswer(profileAnswer);
            log.info("사용자 선호도 수정 완료 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
            
            return profileAnswer;
        } catch (Exception e) {
            log.error("사용자 선호도 수정 중 오류 발생 - userId: {}, error: {}", user.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.PROFILE_ANSWER_UPDATE_FAILED);
        }
    }
    
    /**
     * 사용자 선호도를 생성하거나 수정하는 메서드
     * 
     * @param user 사용자 객체
     * @param requestDto 선호도 데이터
     * @return 생성되거나 수정된 선호도 엔티티
     */
    @Transactional
    public ProfileAnswer saveOrUpdateProfileAnswer(User user, ProfileAnswerRequestDto requestDto) {
        // ===== 내부로직: 조건부 생성/수정 처리 =====
        if (profileAnswerRepository.existsByUser(user)) {
            return updateProfileAnswer(user, requestDto);
        } else {
            return createProfileAnswer(user, requestDto);
        }
    }
    
    /**
     * 사용자 선호도를 삭제하는 메서드
     * 
     * @param user 사용자 객체
     * @throws IllegalArgumentException 선호도를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteProfileAnswer(User user) {
        log.info("=== 사용자 선호도 삭제 시작 ===");
        log.info("삭제 요청 사용자 ID: {}", user.getId());
        
        // ===== 내부로직: 선호도 및 연관 임베딩 삭제 =====
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("삭제할 사용자 선호도를 찾을 수 없음 - userId: {}", user.getId());
                    return new BusinessException(ErrorCode.PROFILE_ANSWER_NOT_FOUND);
                });
        
        try {
            profileAnswerRepository.delete(profileAnswer);
            recommendationService.deleteUserEmbedding(user.getId());
            log.info("사용자 선호도 삭제 완료 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        } catch (Exception e) {
            log.error("사용자 선호도 삭제 중 오류 발생 - userId: {}, error: {}", user.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.PROFILE_ANSWER_DELETE_FAILED);
        }
    }
    
    /**
     * 특정 여행 스타일을 선호하는 사용자들의 선호도를 조회하는 메서드
     * 
     * @param isChill 느긋한 스타일 선호 여부
     * @param isBusy 바쁜 스타일 선호 여부
     * @param isFlex 유연한 스타일 선호 여부
     * @return 해당 스타일을 선호하는 사용자들의 선호도 목록
     */
    public List<ProfileAnswerResponseDto> getProfileAnswersByScheduleStyle(Boolean isChill, Boolean isBusy, Boolean isFlex) {
        List<ProfileAnswer> profileAnswers = profileAnswerRepository.findByScheduleStyle(isChill, isBusy, isFlex);
        
        return profileAnswers.stream()
                .map(ProfileAnswerResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 술자리 선호도를 가진 사용자들의 선호도를 조회하는 메서드
     * 
     * @param isAlchol3 술 좋아하는 여부
     * @param isAlchol2 분위기상 한두 잔 정도 여부
     * @param isAlchol1 술 즐기지 않는 여부
     * @return 해당 선호도를 가진 사용자들의 선호도 목록
     */
    public List<ProfileAnswerResponseDto> getProfileAnswersByDrinkingPreference(Boolean isAlchol3, Boolean isAlchol2, Boolean isAlchol1) {
        List<ProfileAnswer> profileAnswers = profileAnswerRepository.findByDrinkingPreference(isAlchol3, isAlchol2, isAlchol1);
        
        return profileAnswers.stream()
                .map(ProfileAnswerResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * ProfileAnswer에서 사용자 임베딩 벡터를 생성하는 메서드
     * 24개의 boolean 값을 30차원 벡터로 변환
     * 
     * @param profileAnswer 사용자 선호도 엔티티
     */
    private void createUserEmbeddingFromProfileAnswer(ProfileAnswer profileAnswer) {
        log.info("=== 사용자 임베딩 생성/업데이트 시작 ===");
        log.info("임베딩 생성 요청 사용자 ID: {}", profileAnswer.getUser().getId());
        
        try {
            // 24개의 boolean 값을 1,0으로 변환하여 벡터 생성
            String embedding = convertProfileAnswerToEmbedding(profileAnswer);
            log.info("임베딩 벡터 생성 완료 - userId: {}, vectorLength: {}, vector: {}", 
                    profileAnswer.getUser().getId(), embedding.length(), embedding);
            
            // RecommendationService를 통해 임베딩 저장/업데이트
            recommendationService.createOrUpdateUserEmbedding(profileAnswer.getUser().getId(), embedding);
            
            log.info("사용자 임베딩 생성/업데이트 완료 - userId: {}", profileAnswer.getUser().getId());
            
        } catch (Exception e) {
            log.error("사용자 임베딩 생성 중 오류 발생 - userId: {}, error: {}, stackTrace: {}", 
                    profileAnswer.getUser().getId(), e.getMessage(), e.getStackTrace(), e);
            throw new BusinessException(ErrorCode.EMBEDDING_GENERATION_FAILED);
        }
    }
    
    /**
     * ProfileAnswer의 boolean 값들을 30차원 벡터 문자열로 변환
     * 
     * @param profileAnswer 사용자 선호도 엔티티
     * @return 30차원 벡터 문자열 (예: "[1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,0,0,0]")
     */
    private String convertProfileAnswerToEmbedding(ProfileAnswer profileAnswer) {
        // 24개의 boolean 값을 1,0으로 변환
        int[] vector = new int[30];
        
        // 🍶 술 관련 선호도 (3개)
        vector[0] = profileAnswer.getIsAlchol3() != null && profileAnswer.getIsAlchol3() ? 1 : 0;
        vector[1] = profileAnswer.getIsAlchol2() != null && profileAnswer.getIsAlchol2() ? 1 : 0;
        vector[2] = profileAnswer.getIsAlchol1() != null && profileAnswer.getIsAlchol1() ? 1 : 0;
        
        // 🤝 성격 관련 선호도 (6개)
        vector[3] = profileAnswer.getIsSmoker() != null && profileAnswer.getIsSmoker() ? 1 : 0;
        vector[4] = profileAnswer.getIsFriendly() != null && profileAnswer.getIsFriendly() ? 1 : 0;
        vector[5] = profileAnswer.getIsQuiet() != null && profileAnswer.getIsQuiet() ? 1 : 0;
        vector[6] = profileAnswer.getIsLead() != null && profileAnswer.getIsLead() ? 1 : 0;
        vector[7] = profileAnswer.getIsParty() != null && profileAnswer.getIsParty() ? 1 : 0;
        vector[8] = profileAnswer.getIsSearch() != null && profileAnswer.getIsSearch() ? 1 : 0;
        vector[9] = profileAnswer.getIsListen() != null && profileAnswer.getIsListen() ? 1 : 0;
        
        // 🏞 활동 관련 선호도 (6개)
        vector[10] = profileAnswer.getIsSee() != null && profileAnswer.getIsSee() ? 1 : 0;
        vector[11] = profileAnswer.getIsCafe() != null && profileAnswer.getIsCafe() ? 1 : 0;
        vector[12] = profileAnswer.getIsTaste() != null && profileAnswer.getIsTaste() ? 1 : 0;
        vector[13] = profileAnswer.getIsPicture() != null && profileAnswer.getIsPicture() ? 1 : 0;
        vector[14] = profileAnswer.getIsShopping() != null && profileAnswer.getIsShopping() ? 1 : 0;
        vector[15] = profileAnswer.getIsOutdoor() != null && profileAnswer.getIsOutdoor() ? 1 : 0;
        
        // 💤 여행 스타일 관련 선호도 (3개)
        vector[16] = profileAnswer.getIsChill() != null && profileAnswer.getIsChill() ? 1 : 0;
        vector[17] = profileAnswer.getIsBusy() != null && profileAnswer.getIsBusy() ? 1 : 0;
        vector[18] = profileAnswer.getIsFlex() != null && profileAnswer.getIsFlex() ? 1 : 0;
        
        // 🌆 여행지 유형 관련 선호도 (4개)
        vector[19] = profileAnswer.getIsCity() != null && profileAnswer.getIsCity() ? 1 : 0;
        vector[20] = profileAnswer.getIsHeal() != null && profileAnswer.getIsHeal() ? 1 : 0;
        vector[21] = profileAnswer.getIsBeach() != null && profileAnswer.getIsBeach() ? 1 : 0;
        vector[22] = profileAnswer.getIsMountain() != null && profileAnswer.getIsMountain() ? 1 : 0;
        
        // 나머지 7차원은 0으로 설정 (확장성을 위해)
        for (int i = 23; i < 30; i++) {
            vector[i] = 0;
        }
        
        // 배열을 문자열로 변환
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        
        return sb.toString();
    }
} 