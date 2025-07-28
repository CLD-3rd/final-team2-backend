package com.goteego.feed.domain;

import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 피드 엔티티 클래스
 * 사용자가 작성한 여행 후기나 경험을 공유하는 게시글을 나타냄
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feeds")
public class Feed extends BaseEntity {
    
    /**
     * 피드 고유 식별자 (Primary Key)
     * 자동 증가하는 ID 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;
    
    /**
     * 피드 작성자의 사용자 ID
     * 외래키로 사용되며, User 엔티티와 연결됨
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 피드 제목
     * 최대 200자까지 입력 가능
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 피드 내용
     * TEXT 타입으로 긴 텍스트 저장 가능
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 피드에 첨부된 이미지 URL
     * 최대 500자까지 저장 가능
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    /**
     * 여행한 장소 또는 위치 정보
     * 최대 100자까지 입력 가능
     */
    @Column(name = "location", length = 100)
    private String location;
    
    /**
     * 피드 조회수
     * 기본값은 0으로 설정
     */
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    /**
     * 배지 요청 여부
     * 기본값은 false로 설정
     */
    @Column(name = "badge_request")
    private Boolean badgeRequest = false;
    
    /**
     * 피드 생성 시간
     * JPA Auditing을 통해 자동으로 설정됨
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 피드 수정 시간
     * 실제 수정이 발생했을 때만 업데이트됨
     */
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    /**
     * 피드 생성 빌더 메서드
     * 
     * @param userId 피드 작성자 ID
     * @param title 피드 제목
     * @param content 피드 내용
     * @param imageUrl 이미지 URL
     * @param location 위치 정보
     */
    @Builder
    public Feed(Long userId, String title, String content, String imageUrl, String location, Boolean badgeRequest) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
        this.badgeRequest = badgeRequest != null ? badgeRequest : false;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = null; // 생성 시에는 null로 설정
    }
    
    /**
     * 피드 정보 수정 메서드
     * 
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param imageUrl 수정할 이미지 URL
     * @param location 수정할 위치 정보
     * @param badgeRequest 수정할 배지 요청 여부
     */
    public void update(String title, String content, String imageUrl, String location, Boolean badgeRequest) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
        this.badgeRequest = badgeRequest != null ? badgeRequest : this.badgeRequest;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 조회수 증가 메서드
     * 피드 상세 조회 시 호출됨
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    /**
     * 피드 작성자 확인 메서드
     * 
     * @param userId 확인할 사용자 ID
     * @return 해당 사용자가 피드 작성자인지 여부
     */
    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }
} 