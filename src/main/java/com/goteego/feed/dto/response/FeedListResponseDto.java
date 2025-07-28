package com.goteego.feed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 피드 목록 응답 DTO 클래스
 * 페이징된 피드 목록과 페이지 정보를 클라이언트에게 전달하는 데이터 전송 객체
 */
@Getter
@Builder
public class FeedListResponseDto {
    
    /**
     * 피드 목록
     */
    private List<FeedResponseDto> feeds;
    
    /**
     * 페이지 정보
     */
    private PageInfoDto pageInfo;
    
    /**
     * 페이지 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class PageInfoDto {
        /**
         * 현재 페이지 번호 (0부터 시작)
         */
        private int currentPage;
        
        /**
         * 페이지당 항목 수
         */
        private int pageSize;
        
        /**
         * 전체 페이지 수
         */
        private int totalPages;
        
        /**
         * 전체 피드 개수
         */
        private long totalElements;
    }
} 