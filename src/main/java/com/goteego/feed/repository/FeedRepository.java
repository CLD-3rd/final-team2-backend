package com.goteego.feed.repository;

import com.goteego.feed.domain.Feed;
import com.goteego.global.domain.enumerate.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * 피드 목록을 검색하는 쿼리 메소드입니다.
     * 주어진 제목, 작성자, 위치에 맞는 피드들을 조회하고, 페이징 처리하여 반환합니다.
     *
     * @param title    검색할 제목 (부분 일치 검색)
     * @param author   검색할 작성자 이름 (부분 일치 검색)
     * @param location 검색할 위치 (정확히 일치)
     * @param pageable 페이징 정보를 포함한 객체 (페이지 번호, 페이지 크기 등)
     * @return Page<Feed> 조건에 맞는 피드 목록과 페이징 정보가 포함된 페이지 객체
     */
//    @EntityGraph(attributePaths = {"author"})
//    @Query("""
//                SELECT f FROM Feed f
//                WHERE (:title IS NULL OR f.title LIKE CONCAT('%', :title, '%'))
//                AND (:author IS NULL OR f.author.nickname LIKE CONCAT('%', :author, '%'))
//                AND (:location IS NULL OR f.location = :location)
//            """)
//    Page<Feed> getFeedsWithCondition(
//            @Param("title") String title,
//            @Param("author") String author,
//            @Param("location") Location location,
//            Pageable pageable
//    );
    @EntityGraph(attributePaths = {"author"})
    @Query("""
        SELECT f FROM Feed f
        WHERE (:title IS NULL OR f.title LIKE CONCAT('%', CAST(:title AS String), '%'))
        AND (:author IS NULL OR f.author.nickname LIKE CONCAT('%', CAST(:author AS String), '%'))
        AND (:location IS NULL OR f.location = :location)
    """)
    Page<Feed> getFeedsWithCondition(
            @Param("title") String title,
            @Param("author") String author,
            @Param("location") Location location,
            Pageable pageable
    );
} 