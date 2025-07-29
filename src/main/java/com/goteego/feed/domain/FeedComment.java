package com.goteego.feed.domain;

import com.goteego.global.domain.BaseEntity;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 피드 코멘트 엔티티 클래스
 * 피드에 달리는 댓글을 나타내는 엔티티
 */
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_comments")
public class FeedComment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    
    /**
     * 코멘트가 달린 피드 (객체 참조 방식)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;
    
    /**
     * 코멘트 작성자 (객체 참조 방식)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    /**
     * 코멘트 생성 빌더 메서드
     */
    @Builder
    public FeedComment(Feed feed, User author, String content) {
        this.feed = feed;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = null;
    }
    
    /**
     * 코멘트 내용 수정 메서드
     */
    public void update(String content) {
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 코멘트 작성자 확인 메서드
     */
    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
} 