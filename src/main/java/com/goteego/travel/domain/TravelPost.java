package com.goteego.travel.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 게시글 도메인 엔티티
 * 사용자가 작성한 여행 모집 게시글을 나타내는 도메인 객체
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_posts")
public class TravelPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_post_id")
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "chat_room_id")
    private Long chatRoomId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "start_time")
    private LocalDate startTime;
    
    @Column(name = "end_time")
    private LocalDate endTime;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "recuit_limit")
    private Integer recuitLimit;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    @Column(name = "is_add_recruit")
    private Boolean isAddRecruit = false;
    
    /**
     * 여행 게시글 타입 열거형
     */
    public enum PostType {
        /** 사전 모집 */
        BEFORE, 
        /** 현지 모집 */
        NOW
    }
    
    @Builder
    public TravelPost(Long userId, Long chatRoomId, String title, String content, 
                     LocalDate startTime, LocalDate endTime, String imageUrl, 
                     Integer recuitLimit, PostType postType, Boolean isAddRecruit) {
        this.userId = userId;
        this.chatRoomId = chatRoomId;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.recuitLimit = recuitLimit;
        this.postType = postType;
        this.isAddRecruit = isAddRecruit;
    }
    
    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    /**
     * 게시글 수정
     */
    public void update(String title, String content, LocalDate startTime, LocalDate endTime, 
                      String imageUrl, Integer recuitLimit, PostType postType, Boolean isAddRecruit) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.recuitLimit = recuitLimit;
        this.postType = postType;
        this.isAddRecruit = isAddRecruit;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 작성자 확인
     */
    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }
} 