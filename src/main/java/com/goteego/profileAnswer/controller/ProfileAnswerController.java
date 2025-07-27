package com.goteego.profileAnswer.controller;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import com.goteego.profileAnswer.dto.ProfileAnswerRequestDto;
import com.goteego.profileAnswer.dto.ProfileAnswerResponseDto;
import com.goteego.profileAnswer.service.ProfileAnswerService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 여행 취향 설문 컨트롤러
 * 여행 취향 설문 관련 REST API 엔드포인트를 제공하는 컨트롤러
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/profile-answers")
@RequiredArgsConstructor
public class ProfileAnswerController {
    
    private final ProfileAnswerService profileAnswerService;
    
    /**
     * 현재 로그인한 사용자의 여행 취향 설문 조회
     * 
     * @param user 현재 로그인한 사용자
     * @return 사용자의 여행 취향 설문
     */
    @GetMapping("/my")
    public ResponseEntity<ProfileAnswerResponseDto> getMyProfileAnswer(@AuthenticationPrincipal User user) {
        ProfileAnswerResponseDto profileAnswer = profileAnswerService.getProfileAnswer(user.getId());
        
        log.info("내 여행 취향 설문 조회 - userId: {}", user.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 특정 사용자의 여행 취향 설문 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 여행 취향 설문
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileAnswerResponseDto> getProfileAnswer(@PathVariable("userId") Long userId) {
        ProfileAnswerResponseDto profileAnswer = profileAnswerService.getProfileAnswer(userId);
        
        log.info("사용자 여행 취향 설문 조회 - userId: {}", userId);
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 여행 취향 설문 존재 여부 확인
     * 
     * @param user 현재 로그인한 사용자
     * @return 설문 존재 여부
     */
    @GetMapping("/my/exists")
    public ResponseEntity<Boolean> checkMyProfileAnswerExists(@AuthenticationPrincipal User user) {
        boolean exists = profileAnswerService.existsByUserId(user.getId());
        
        log.info("내 여행 취향 설문 존재 여부 확인 - userId: {}, exists: {}", user.getId(), exists);
        
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 여행 취향 설문 완료 여부 확인
     * 
     * @param user 현재 로그인한 사용자
     * @return 설문 완료 여부
     */
    @GetMapping("/my/completed")
    public ResponseEntity<Boolean> checkMyProfileAnswerCompleted(@AuthenticationPrincipal User user) {
        boolean completed = profileAnswerService.isCompletedByUserId(user.getId());
        
        log.info("내 여행 취향 설문 완료 여부 확인 - userId: {}, completed: {}", user.getId(), completed);
        
        return ResponseEntity.ok(completed);
    }
    
    /**
     * 여행 취향 설문 생성
     * 
     * @param requestDto 설문 요청 데이터
     * @param user 현재 로그인한 사용자
     * @return 생성된 설문
     */
    @PostMapping
    public ResponseEntity<ProfileAnswer> createProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.createProfileAnswer(user.getId(), requestDto);
        
        log.info("여행 취향 설문 생성 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 여행 취향 설문 수정
     * 
     * @param requestDto 수정할 설문 데이터
     * @param user 현재 로그인한 사용자
     * @return 수정된 설문
     */
    @PutMapping
    public ResponseEntity<ProfileAnswer> updateProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.updateProfileAnswer(user.getId(), requestDto);
        
        log.info("여행 취향 설문 수정 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 여행 취향 설문 생성 또는 수정
     * 
     * @param requestDto 설문 데이터
     * @param user 현재 로그인한 사용자
     * @return 생성되거나 수정된 설문
     */
    @PostMapping("/save-or-update")
    public ResponseEntity<ProfileAnswer> saveOrUpdateProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.saveOrUpdateProfileAnswer(user.getId(), requestDto);
        
        log.info("여행 취향 설문 저장 또는 수정 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 여행 취향 설문 삭제
     * 
     * @param user 현재 로그인한 사용자
     * @return 삭제 결과 메시지
     */
    @DeleteMapping
    public ResponseEntity<String> deleteProfileAnswer(@AuthenticationPrincipal User user) {
        profileAnswerService.deleteProfileAnswer(user.getId());
        
        log.info("여행 취향 설문 삭제 - userId: {}", user.getId());
        
        return ResponseEntity.ok("여행 취향 설문이 삭제되었습니다.");
    }
    
    /**
     * 특정 여행 스타일을 선호하는 사용자들의 설문 조회
     * 
     * @param scheduleStyle 조회할 여행 스타일
     * @return 해당 스타일을 선호하는 사용자들의 설문 목록
     */
    @GetMapping("/by-schedule-style")
    public ResponseEntity<List<ProfileAnswerResponseDto>> getProfileAnswersByScheduleStyle(
            @RequestParam("scheduleStyle") String scheduleStyle) {
        
        List<ProfileAnswerResponseDto> profileAnswers = profileAnswerService.getProfileAnswersByScheduleStyle(scheduleStyle);
        
        log.info("여행 스타일별 설문 조회 - scheduleStyle: {}, count: {}", scheduleStyle, profileAnswers.size());
        
        return ResponseEntity.ok(profileAnswers);
    }
    
    /**
     * 특정 술자리 선호도를 가진 사용자들의 설문 조회
     * 
     * @param drinkingPreference 조회할 술자리 선호도
     * @return 해당 선호도를 가진 사용자들의 설문 목록
     */
    @GetMapping("/by-drinking-preference")
    public ResponseEntity<List<ProfileAnswerResponseDto>> getProfileAnswersByDrinkingPreference(
            @RequestParam("drinkingPreference") String drinkingPreference) {
        
        List<ProfileAnswerResponseDto> profileAnswers = profileAnswerService.getProfileAnswersByDrinkingPreference(drinkingPreference);
        
        log.info("술자리 선호도별 설문 조회 - drinkingPreference: {}, count: {}", drinkingPreference, profileAnswers.size());
        
        return ResponseEntity.ok(profileAnswers);
    }
} 