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
 * 피드 코멘트 엔티티 클래스
 * 피드에 달리는 댓글을 나타내는 엔티티
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_comments")
public class FeedComment extends BaseEntity {
    
    /**
     * 코멘트 고유 식별자 (Primary Key)
     * 자동 증가하는 ID 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    
    /**
     * 코멘트가 달린 피드의 ID
     * 외래키로 사용되며, Feed 엔티티와 연결됨
     */
    @Column(name = "feed_id", nullable = false)
    private Long feedId;
    
    /**
     * 코멘트 작성자의 사용자 ID
     * 외래키로 사용되며, User 엔티티와 연결됨
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 코멘트 내용
     * 최대 1000자까지 입력 가능
     */
    @Column(nullable = false, length = 1000)
    private String content;
    
    /**
     * 코멘트 생성 시간
     * JPA Auditing을 통해 자동으로 설정됨
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 코멘트 수정 시간
     * 실제 수정이 발생했을 때만 업데이트됨
     */
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    /**
     * 코멘트 생성 빌더 메서드
     * 
     * @param feedId 코멘트가 달릴 피드 ID
     * @param userId 코멘트 작성자 ID
     * @param content 코멘트 내용
     */
    @Builder
    public FeedComment(Long feedId, Long userId, String content) {
        this.feedId = feedId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = null; // 생성 시에는 null로 설정
    }
    
    /**
     * 코멘트 내용 수정 메서드
     * 
     * @param content 수정할 내용
     */
    public void update(String content) {
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 코멘트 작성자 확인 메서드
     * 
     * @param userId 확인할 사용자 ID
     * @return 해당 사용자가 코멘트 작성자인지 여부
     */
    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }
} 