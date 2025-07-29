package com.goteego.feed.repository;

import com.goteego.feed.domain.Feed;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.feed.domain.enumerate.FeedSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 피드 리포지토리 인터페이스
 * Feed 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리
 */
@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    
    /**
     * 모든 피드를 생성일 기준 내림차순으로 페이징하여 조회 (Fetch Join으로 N+1 문제 해결)
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author ORDER BY f.createdAt DESC")
    Page<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 특정 작성자의 닉네임으로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.author.nickname LIKE %:author% OR f.author.nickname = :author ORDER BY f.createdAt DESC")
    Page<Feed> findByAuthorNicknameContainingOrderByCreatedAtDesc(@Param("author") String author, Pageable pageable);
    
    /**
     * 특정 위치로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.location = :location ORDER BY f.createdAt DESC")
    Page<Feed> findByLocationOrderByCreatedAtDesc(@Param("location") Location location, Pageable pageable);
    
    /**
     * 제목으로 피드를 검색하여 생성일 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.title LIKE %:title% ORDER BY f.createdAt DESC")
    Page<Feed> findByTitleContainingOrderByCreatedAtDesc(@Param("title") String title, Pageable pageable);
    
    /**
     * 모든 피드를 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author ORDER BY f.viewCount DESC")
    Page<Feed> findAllByOrderByViewCountDesc(Pageable pageable);
    
    /**
     * 특정 작성자의 닉네임으로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.author.nickname LIKE %:author% OR f.author.nickname = :author ORDER BY f.viewCount DESC")
    Page<Feed> findByAuthorNicknameContainingOrderByViewCountDesc(@Param("author") String author, Pageable pageable);
    
    /**
     * 특정 위치로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.location = :location ORDER BY f.viewCount DESC")
    Page<Feed> findByLocationOrderByViewCountDesc(@Param("location") Location location, Pageable pageable);
    
    /**
     * 제목으로 피드를 검색하여 조회수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.title LIKE %:title% ORDER BY f.viewCount DESC")
    Page<Feed> findByTitleContainingOrderByViewCountDesc(@Param("title") String title, Pageable pageable);
    
    /**
     * 모든 피드를 좋아요수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author ORDER BY f.likeCount DESC")
    Page<Feed> findAllByOrderByLikeCountDesc(Pageable pageable);
    
    /**
     * 특정 작성자의 닉네임으로 피드를 검색하여 좋아요수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.author.nickname LIKE %:author% OR f.author.nickname = :author ORDER BY f.likeCount DESC")
    Page<Feed> findByAuthorNicknameContainingOrderByLikeCountDesc(@Param("author") String author, Pageable pageable);
    
    /**
     * 특정 위치로 피드를 검색하여 좋아요수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.location = :location ORDER BY f.likeCount DESC")
    Page<Feed> findByLocationOrderByLikeCountDesc(@Param("location") Location location, Pageable pageable);
    
    /**
     * 제목으로 피드를 검색하여 좋아요수 기준 내림차순으로 페이징하여 조회
     */
    @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.title LIKE %:title% ORDER BY f.likeCount DESC")
    Page<Feed> findByTitleContainingOrderByLikeCountDesc(@Param("title") String title, Pageable pageable);
} 