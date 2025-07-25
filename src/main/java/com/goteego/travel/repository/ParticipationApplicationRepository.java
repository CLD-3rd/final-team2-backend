package com.goteego.travel.repository;

import com.goteego.travel.domain.ParticipationApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 참가 신청 Repository
 * participation_application 테이블에 대한 데이터 접근을 담당
 * 참가 신청 조회, 상태 변경 등의 기능을 제공
 */
@Repository
public interface ParticipationApplicationRepository extends JpaRepository<ParticipationApplication, Long> {
    
    /**
     * 특정 여행 게시글의 특정 사용자 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @param userId 사용자 ID
     * @return 참가 신청 정보 (Optional - 없을 수 있음)
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId AND pa.userId = :userId")
    Optional<ParticipationApplication> findByTravelPostIdAndUserId(@Param("travelPostId") Long travelPostId, 
                                                                  @Param("userId") Long userId);
    
    /**
     * 특정 여행 게시글의 모든 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 참가 신청 목록
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId")
    List<ParticipationApplication> findByTravelPostId(@Param("travelPostId") Long travelPostId);
    
    /**
     * 특정 여행 게시글의 승인된 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 승인된 참가 신청 목록
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId AND pa.status = 'APPROVED'")
    List<ParticipationApplication> findApprovedByTravelPostId(@Param("travelPostId") Long travelPostId);
    
    /**
     * 특정 여행 게시글의 대기 중인 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 대기 중인 참가 신청 목록
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId AND pa.status = 'PENDING'")
    List<ParticipationApplication> findPendingByTravelPostId(@Param("travelPostId") Long travelPostId);
    
    /**
     * 참가 신청 상태 변경
     * 작성자가 참가 신청을 승인하거나 거절할 때 사용
     * 
     * @param travelPostId 여행 게시글 ID
     * @param userId 참가 신청한 사용자 ID
     * @param status 변경할 상태 (APPROVED 또는 REJECTED)
     */
    @Modifying
    @Transactional
    @Query("UPDATE ParticipationApplication pa SET pa.status = :status WHERE pa.travelPostId = :travelPostId AND pa.userId = :userId")
    void updateStatusByTravelPostIdAndUserId(@Param("travelPostId") Long travelPostId, 
                                            @Param("userId") Long userId, 
                                            @Param("status") ParticipationApplication.Status status);
    
    /**
     * 특정 여행 게시글의 참가 신청 개수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @param status 참가 신청 상태
     * @return 해당 상태의 참가 신청 개수
     */
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId AND pa.status = :status")
    Long countByTravelPostIdAndStatus(@Param("travelPostId") Long travelPostId, 
                                     @Param("status") ParticipationApplication.Status status);
    
    /**
     * 중복 신청 확인
     * 
     * @param travelPostId 여행 게시글 ID
     * @param userId 사용자 ID
     * @return 중복 신청 여부
     */
    @Query("SELECT COUNT(pa) > 0 FROM ParticipationApplication pa WHERE pa.travelPostId = :travelPostId AND pa.userId = :userId")
    boolean existsByTravelPostIdAndUserId(@Param("travelPostId") Long travelPostId, 
                                         @Param("userId") Long userId);
} 