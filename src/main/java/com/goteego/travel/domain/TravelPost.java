package com.goteego.travel.domain;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.global.domain.BaseEntity;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_posts")
public class TravelPost extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;

    private String title;
    private String content;
    private LocalDate startTime;
    private LocalDate endTime;
    private Integer recruitLimit;
    private Long viewCount = 0L;
    private Boolean isAddRecruit = false;

    
    @Builder
    public TravelPost(User user, ChatRoom chatRoom, String title, String content,
                     LocalDate startTime, LocalDate endTime, String imageUrl, 
                     Integer recruitLimit, PostType postType, Boolean isAddRecruit) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.recruitLimit = recruitLimit;
        this.postType = postType;
        this.isAddRecruit = isAddRecruit;
    }


    //=========비즈니스 로직==========//

    // 게시글 수정
    public void update(String title, String content, LocalDate startTime, LocalDate endTime,
                       String imageUrl, Integer recruitLimit, PostType postType, Boolean isAddRecruit) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.recruitLimit = recruitLimit;
        this.postType = postType;
        this.isAddRecruit = isAddRecruit;
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 작성자 본인 확인
    public boolean isAuthor(Long userId) {
        return this.user.getId().equals(userId);
    }


} 