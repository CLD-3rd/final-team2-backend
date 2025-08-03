package com.goteego.profileAnswer.repository;

import com.goteego.profileAnswer.domain.ProfileAnswer;
import com.goteego.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 선호도 리포지토리 인터페이스
 * ProfileAnswer 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리
 *
 * @author GotEEgo Team
 * @version 1.0
 */
@Repository
public interface ProfileAnswerRepository extends JpaRepository<ProfileAnswer, Long> {

    /**
     * 사용자로 선호도를 조회
     *
     * @param user 조회할 사용자
     * @return 해당 사용자의 선호도 (Optional)
     */
    Optional<ProfileAnswer> findByUser(User user);

    /**
     * userId를 기반으로 ProfileAnswer 조회
     *
     * @param userId 사용자 ID
     * @return Optional<ProfileAnswer>
     */
    Optional<ProfileAnswer> findByUserId(Long userId);

    /**
     * 사용자로 선호도 존재 여부를 확인
     *
     * @param user 확인할 사용자
     * @return 선호도 존재 여부
     */
    boolean existsByUser(User user);

    /**
     * 특정 여행 스타일을 선호하는 사용자들의 선호도를 조회
     *
     * @param isChill 느긋한 스타일 선호 여부
     * @param isBusy  바쁜 스타일 선호 여부
     * @param isFlex  유연한 스타일 선호 여부
     * @return 해당 스타일을 선호하는 사용자들의 선호도 목록
     */
    @Query("SELECT p FROM ProfileAnswer p WHERE " +
            "(:isChill IS NULL OR p.isChill = :isChill) AND " +
            "(:isBusy IS NULL OR p.isBusy = :isBusy) AND " +
            "(:isFlex IS NULL OR p.isFlex = :isFlex)")
    List<ProfileAnswer> findByScheduleStyle(
            @Param("isChill") Boolean isChill,
            @Param("isBusy") Boolean isBusy,
            @Param("isFlex") Boolean isFlex);

    /**
     * 특정 술자리 선호도를 가진 사용자들의 선호도를 조회
     *
     * @param isAlchol3 술 좋아하는 여부
     * @param isAlchol2 분위기상 한두 잔 정도 여부
     * @param isAlchol1 술 즐기지 않는 여부
     * @return 해당 선호도를 가진 사용자들의 선호도 목록
     */
    @Query("SELECT p FROM ProfileAnswer p WHERE " +
            "(:isAlchol3 IS NULL OR p.isAlchol3 = :isAlchol3) AND " +
            "(:isAlchol2 IS NULL OR p.isAlchol2 = :isAlchol2) AND " +
            "(:isAlchol1 IS NULL OR p.isAlchol1 = :isAlchol1)")
    List<ProfileAnswer> findByDrinkingPreference(
            @Param("isAlchol3") Boolean isAlchol3,
            @Param("isAlchol2") Boolean isAlchol2,
            @Param("isAlchol1") Boolean isAlchol1);
} 