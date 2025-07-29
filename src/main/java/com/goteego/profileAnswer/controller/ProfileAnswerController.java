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
 * 사용자 선호도 컨트롤러
 * 사용자 선호도 관련 REST API 엔드포인트를 제공하는 컨트롤러
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
     * 현재 로그인한 사용자의 선호도 조회
     * 
     * @param user 현재 로그인한 사용자
     * @return 사용자의 선호도
     */
    @GetMapping("/my")
    public ResponseEntity<ProfileAnswerResponseDto> getMyProfileAnswer(@AuthenticationPrincipal User user) {
        ProfileAnswerResponseDto profileAnswer = profileAnswerService.getProfileAnswer(user);
        
        log.info("내 사용자 선호도 조회 - userId: {}", user.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 사용자 선호도 존재 여부 확인
     * 
     * @param user 현재 로그인한 사용자
     * @return 선호도 존재 여부
     */
    @GetMapping("/my/exists")
    public ResponseEntity<Boolean> checkMyProfileAnswerExists(@AuthenticationPrincipal User user) {
        boolean exists = profileAnswerService.existsByUser(user);
        
        log.info("내 사용자 선호도 존재 여부 확인 - userId: {}, exists: {}", user.getId(), exists);
        
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 사용자 선호도 생성
     * 
     * @param requestDto 선호도 요청 데이터
     * @param user 현재 로그인한 사용자
     * @return 생성된 선호도
     */
    @PostMapping
    public ResponseEntity<ProfileAnswer> createProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.createProfileAnswer(user, requestDto);
        
        log.info("사용자 선호도 생성 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 사용자 선호도 수정
     * 
     * @param requestDto 수정할 선호도 데이터
     * @param user 현재 로그인한 사용자
     * @return 수정된 선호도
     */
    @PutMapping
    public ResponseEntity<ProfileAnswer> updateProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.updateProfileAnswer(user, requestDto);
        
        log.info("사용자 선호도 수정 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 사용자 선호도 생성 또는 수정
     * 
     * @param requestDto 선호도 데이터
     * @param user 현재 로그인한 사용자
     * @return 생성되거나 수정된 선호도
     */
    @PostMapping("/save-or-update")
    public ResponseEntity<ProfileAnswer> saveOrUpdateProfileAnswer(
            @RequestBody ProfileAnswerRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        ProfileAnswer profileAnswer = profileAnswerService.saveOrUpdateProfileAnswer(user, requestDto);
        
        log.info("사용자 선호도 저장 또는 수정 - userId: {}, answerId: {}", user.getId(), profileAnswer.getId());
        
        return ResponseEntity.ok(profileAnswer);
    }
    
    /**
     * 사용자 선호도 삭제
     * 
     * @param user 현재 로그인한 사용자
     * @return 삭제 결과 메시지
     */
    @DeleteMapping
    public ResponseEntity<String> deleteProfileAnswer(@AuthenticationPrincipal User user) {
        profileAnswerService.deleteProfileAnswer(user);
        
        log.info("사용자 선호도 삭제 - userId: {}", user.getId());
        
        return ResponseEntity.ok("사용자 선호도가 삭제되었습니다.");
    }
    
    /**
     * 특정 여행 스타일을 선호하는 사용자들의 선호도 조회
     * 
     * @param isChill 느긋한 스타일 선호 여부
     * @param isBusy 바쁜 스타일 선호 여부
     * @param isFlex 유연한 스타일 선호 여부
     * @return 해당 스타일을 선호하는 사용자들의 선호도 목록
     */
    @GetMapping("/by-schedule-style")
    public ResponseEntity<List<ProfileAnswerResponseDto>> getProfileAnswersByScheduleStyle(
            @RequestParam(value = "isChill", required = false) Boolean isChill,
            @RequestParam(value = "isBusy", required = false) Boolean isBusy,
            @RequestParam(value = "isFlex", required = false) Boolean isFlex) {
        
        List<ProfileAnswerResponseDto> profileAnswers = profileAnswerService.getProfileAnswersByScheduleStyle(isChill, isBusy, isFlex);
        
        log.info("여행 스타일별 선호도 조회 - isChill: {}, isBusy: {}, isFlex: {}, count: {}", 
                isChill, isBusy, isFlex, profileAnswers.size());
        
        return ResponseEntity.ok(profileAnswers);
    }
    
    /**
     * 특정 술자리 선호도를 가진 사용자들의 선호도 조회
     * 
     * @param isAlchol3 술 좋아하는 여부
     * @param isAlchol2 분위기상 한두 잔 정도 여부
     * @param isAlchol1 술 즐기지 않는 여부
     * @return 해당 선호도를 가진 사용자들의 선호도 목록
     */
    @GetMapping("/by-drinking-preference")
    public ResponseEntity<List<ProfileAnswerResponseDto>> getProfileAnswersByDrinkingPreference(
            @RequestParam(value = "isAlchol3", required = false) Boolean isAlchol3,
            @RequestParam(value = "isAlchol2", required = false) Boolean isAlchol2,
            @RequestParam(value = "isAlchol1", required = false) Boolean isAlchol1) {
        
        List<ProfileAnswerResponseDto> profileAnswers = profileAnswerService.getProfileAnswersByDrinkingPreference(isAlchol3, isAlchol2, isAlchol1);
        
        log.info("술자리 선호도별 선호도 조회 - isAlchol3: {}, isAlchol2: {}, isAlchol1: {}, count: {}", 
                isAlchol3, isAlchol2, isAlchol1, profileAnswers.size());
        
        return ResponseEntity.ok(profileAnswers);
    }
} 