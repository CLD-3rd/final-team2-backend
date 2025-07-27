package com.goteego.profileAnswer.repository;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 여행 취향 설문 리포지토리 인터페이스
 * ProfileAnswer 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Repository
public interface ProfileAnswerRepository extends JpaRepository<ProfileAnswer, Long> {
    
    /**
     * 사용자 ID로 여행 취향 설문을 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 여행 취향 설문 (Optional)
     */
    Optional<ProfileAnswer> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 여행 취향 설문 존재 여부를 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 설문 존재 여부
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자 ID로 설문 완료 여부를 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 설문 완료 여부
     */
    @Query("SELECT p.isCompleted FROM ProfileAnswer p WHERE p.userId = :userId")
    Optional<Boolean> findIsCompletedByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 여행 스타일을 선호하는 사용자들의 설문을 조회
     * 
     * @param scheduleStyle 조회할 여행 스타일
     * @return 해당 스타일을 선호하는 사용자들의 설문 목록
     */
    @Query("SELECT p FROM ProfileAnswer p WHERE p.scheduleStyle = :scheduleStyle")
    java.util.List<ProfileAnswer> findByScheduleStyle(@Param("scheduleStyle") String scheduleStyle);
    
    /**
     * 특정 술자리 선호도를 가진 사용자들의 설문을 조회
     * 
     * @param drinkingPreference 조회할 술자리 선호도
     * @return 해당 선호도를 가진 사용자들의 설문 목록
     */
    @Query("SELECT p FROM ProfileAnswer p WHERE p.drinkingPreference = :drinkingPreference")
    java.util.List<ProfileAnswer> findByDrinkingPreference(@Param("drinkingPreference") String drinkingPreference);
} 