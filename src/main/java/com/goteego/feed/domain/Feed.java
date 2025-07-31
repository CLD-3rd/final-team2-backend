package com.goteego.feed.domain;

import com.goteego.global.domain.BaseEntity;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 피드 엔티티 클래스
 * 사용자가 작성한 여행 후기나 경험을 공유하는 게시글을 나타냄
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feeds")
public class Feed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;

    /**
     * 피드 작성자 (객체 참조 방식)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 여행한 장소 또는 위치 정보 (공통 Location enum 사용)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location", length = 50)
    private Location location;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "badge_request")
    private Boolean badgeRequest = false;

    /**
     * 피드에 달린 댓글 목록 (1:N 관계)
     */
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedComment> comments = new ArrayList<>();

    /**
     * 피드 생성 빌더 메서드
     */
    @Builder
    public Feed(User author, String title, String content, String imageUrl, Location location, Boolean badgeRequest) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
        this.badgeRequest = badgeRequest != null ? badgeRequest : false;
    }

    /**
     * 피드 정보 수정 메서드
     */
    public void update(String title, String content, String imageUrl, Location location, Boolean badgeRequest) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (location != null) {
            this.location = location;
        }
        if (badgeRequest != null) {
            this.badgeRequest = badgeRequest;
        }
    }

    /**
     * 조회수 증가 메서드
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 피드 작성자 확인 메서드
     */
    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }

    /**
     * 댓글 추가 메서드
     */
    public void addComment(FeedComment comment) {
        this.comments.add(comment);
        comment.setFeed(this);
    }

    /**
     * 댓글 제거 메서드
     */
    public void removeComment(FeedComment comment) {
        this.comments.remove(comment);
        comment.setFeed(null);
    }
} 