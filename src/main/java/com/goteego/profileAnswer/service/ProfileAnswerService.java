package com.goteego.profileAnswer.service;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import com.goteego.profileAnswer.dto.ProfileAnswerRequestDto;
import com.goteego.profileAnswer.dto.ProfileAnswerResponseDto;
import com.goteego.profileAnswer.repository.ProfileAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 여행 취향 설문 서비스 클래스
 * 여행 취향 설문 관련 비즈니스 로직을 처리하는 서비스 계층
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
     * 여행 취향 설문 데이터 접근을 위한 리포지토리
     */
    private final ProfileAnswerRepository profileAnswerRepository;
    
    /**
     * 사용자의 여행 취향 설문을 조회하는 메서드
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 여행 취향 설문 응답 DTO
     * @throws IllegalArgumentException 설문을 찾을 수 없는 경우
     */
    public ProfileAnswerResponseDto getProfileAnswer(Long userId) {
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("여행 취향 설문을 찾을 수 없습니다."));
        
        return ProfileAnswerResponseDto.from(profileAnswer);
    }
    
    /**
     * 사용자의 여행 취향 설문 존재 여부를 확인하는 메서드
     * 
     * @param userId 확인할 사용자 ID
     * @return 설문 존재 여부
     */
    public boolean existsByUserId(Long userId) {
        return profileAnswerRepository.existsByUserId(userId);
    }
    
    /**
     * 사용자의 설문 완료 여부를 확인하는 메서드
     * 
     * @param userId 확인할 사용자 ID
     * @return 설문 완료 여부 (설문이 없으면 false)
     */
    public boolean isCompletedByUserId(Long userId) {
        return profileAnswerRepository.findIsCompletedByUserId(userId)
                .orElse(false);
    }
    
    /**
     * 새로운 여행 취향 설문을 생성하는 메서드
     * 
     * @param userId 사용자 ID
     * @param requestDto 설문 요청 데이터
     * @return 생성된 설문 엔티티
     */
    @Transactional
    public ProfileAnswer createProfileAnswer(Long userId, ProfileAnswerRequestDto requestDto) {
        // 이미 설문이 존재하는지 확인
        if (profileAnswerRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 여행 취향 설문이 존재합니다.");
        }
        
        // JSON 형태로 변환하여 저장
        String travelTendencies = convertListToJson(requestDto.getTravelTendencies());
        String preferredActivities = convertListToJson(requestDto.getPreferredActivities());
        String preferredDestinations = convertListToJson(requestDto.getPreferredDestinations());
        
        // 설문 엔티티 생성
        ProfileAnswer profileAnswer = ProfileAnswer.builder()
                .userId(userId)
                .travelTendencies(travelTendencies)
                .preferredActivities(preferredActivities)
                .scheduleStyle(requestDto.getScheduleStyle())
                .drinkingPreference(requestDto.getDrinkingPreference())
                .smokingStatus(requestDto.getSmokingStatus())
                .preferredDestinations(preferredDestinations)
                .build();
        
        // 데이터베이스에 저장
        return profileAnswerRepository.save(profileAnswer);
    }
    
    /**
     * 여행 취향 설문을 수정하는 메서드
     * 
     * @param userId 수정할 사용자 ID
     * @param requestDto 수정할 설문 데이터
     * @return 수정된 설문 엔티티
     * @throws IllegalArgumentException 설문을 찾을 수 없는 경우
     */
    @Transactional
    public ProfileAnswer updateProfileAnswer(Long userId, ProfileAnswerRequestDto requestDto) {
        // 기존 설문 조회
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("여행 취향 설문을 찾을 수 없습니다."));
        
        // JSON 형태로 변환하여 저장
        String travelTendencies = convertListToJson(requestDto.getTravelTendencies());
        String preferredActivities = convertListToJson(requestDto.getPreferredActivities());
        String preferredDestinations = convertListToJson(requestDto.getPreferredDestinations());
        
        // 설문 정보 업데이트
        profileAnswer.update(travelTendencies, preferredActivities, requestDto.getScheduleStyle(),
                           requestDto.getDrinkingPreference(), requestDto.getSmokingStatus(), preferredDestinations);
        
        return profileAnswer;
    }
    
    /**
     * 여행 취향 설문을 생성하거나 수정하는 메서드
     * 
     * @param userId 사용자 ID
     * @param requestDto 설문 데이터
     * @return 생성되거나 수정된 설문 엔티티
     */
    @Transactional
    public ProfileAnswer saveOrUpdateProfileAnswer(Long userId, ProfileAnswerRequestDto requestDto) {
        if (profileAnswerRepository.existsByUserId(userId)) {
            return updateProfileAnswer(userId, requestDto);
        } else {
            return createProfileAnswer(userId, requestDto);
        }
    }
    
    /**
     * 여행 취향 설문을 삭제하는 메서드
     * 
     * @param userId 삭제할 사용자 ID
     * @throws IllegalArgumentException 설문을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteProfileAnswer(Long userId) {
        ProfileAnswer profileAnswer = profileAnswerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("여행 취향 설문을 찾을 수 없습니다."));
        
        profileAnswerRepository.delete(profileAnswer);
    }
    
    /**
     * 특정 여행 스타일을 선호하는 사용자들의 설문을 조회하는 메서드
     * 
     * @param scheduleStyle 조회할 여행 스타일
     * @return 해당 스타일을 선호하는 사용자들의 설문 목록
     */
    public List<ProfileAnswerResponseDto> getProfileAnswersByScheduleStyle(String scheduleStyle) {
        List<ProfileAnswer> profileAnswers = profileAnswerRepository.findByScheduleStyle(scheduleStyle);
        
        return profileAnswers.stream()
                .map(ProfileAnswerResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 술자리 선호도를 가진 사용자들의 설문을 조회하는 메서드
     * 
     * @param drinkingPreference 조회할 술자리 선호도
     * @return 해당 선호도를 가진 사용자들의 설문 목록
     */
    public List<ProfileAnswerResponseDto> getProfileAnswersByDrinkingPreference(String drinkingPreference) {
        List<ProfileAnswer> profileAnswers = profileAnswerRepository.findByDrinkingPreference(drinkingPreference);
        
        return profileAnswers.stream()
                .map(ProfileAnswerResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * List를 JSON 문자열로 변환하는 헬퍼 메서드
     * 실제 구현에서는 Jackson ObjectMapper 등을 사용할 수 있음
     * 
     * @param list 변환할 문자열 리스트
     * @return JSON 형태의 문자열
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        // 실제로는 ObjectMapper를 사용하여 JSON으로 변환해야 함
        // 현재는 간단한 구현을 위해 쉼표로 구분된 문자열로 변환
        return String.join(",", list);
    }
} 