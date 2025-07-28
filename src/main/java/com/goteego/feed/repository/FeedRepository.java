package com.goteego.feed.repository;

import com.goteego.feed.domain.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 피드 리포지토리 인터페이스
 * Feed 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    
    /**
     * 모든 피드를 생성일 기준 내림차순으로 페이징하여 조회
     * 
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기 등)
     * @return 페이징된 피드 목록
     */
    @Query("SELECT f FROM Feed f ORDER BY f.createdAt DESC")
    Page<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 특정 작성자의 닉네임으로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     * 닉네임에 검색어가 포함되거나 정확히 일치하는 경우를 모두 포함
     * 
     * @param author 검색할 작성자 닉네임
     * @param pageable 페이징 정보
     * @return 페이징된 피드 목록
     */
    @Query("SELECT f FROM Feed f WHERE f.userId IN " +
           "(SELECT u.id FROM User u WHERE u.nickname LIKE %:author% OR u.nickname = :author) " +
           "ORDER BY f.createdAt DESC")
    Page<Feed> findByAuthorNicknameContainingOrderByCreatedAtDesc(@Param("author") String author, Pageable pageable);
    
    /**
     * 특정 위치로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     * 위치 정보에 검색어가 포함된 피드들을 검색
     * 
     * @param location 검색할 위치 정보
     * @param pageable 페이징 정보
     * @return 페이징된 피드 목록
     */
    @Query("SELECT f FROM Feed f WHERE f.location LIKE %:location% ORDER BY f.createdAt DESC")
    Page<Feed> findByLocationContainingOrderByCreatedAtDesc(@Param("location") String location, Pageable pageable);
    
    // ===== 제목 검색 메서드들 =====
    
    /**
     * 제목으로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.title LIKE %:title% ORDER BY f.createdAt DESC")
    Page<Feed> findByTitleContainingOrderByCreatedAtDesc(@Param("title") String title, Pageable pageable);
    
    /**
     * 제목으로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.title LIKE %:title% ORDER BY f.viewCount DESC")
    Page<Feed> findByTitleContainingOrderByViewCountDesc(@Param("title") String title, Pageable pageable);
    
    // ===== 조회수 기준 정렬 메서드들 =====
    
    /**
     * 모든 피드를 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f ORDER BY f.viewCount DESC")
    Page<Feed> findAllByOrderByViewCountDesc(Pageable pageable);
    
    /**
     * 특정 작성자의 닉네임으로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.userId IN " +
           "(SELECT u.id FROM User u WHERE u.nickname LIKE %:author% OR u.nickname = :author) " +
           "ORDER BY f.viewCount DESC")
    Page<Feed> findByAuthorNicknameContainingOrderByViewCountDesc(@Param("author") String author, Pageable pageable);
    
    /**
     * 특정 위치로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.location LIKE %:location% ORDER BY f.viewCount DESC")
    Page<Feed> findByLocationContainingOrderByViewCountDesc(@Param("location") String location, Pageable pageable);
} 