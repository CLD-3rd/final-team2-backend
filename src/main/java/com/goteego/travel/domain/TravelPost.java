package com.goteego.travel.domain;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.global.domain.BaseEntity;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.travel.TravelPostRequest;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;

    private String title;
    private String content;

    @OneToMany(mappedBy = "travelPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ParticipationApplication> participationApplications = new ArrayList<>();

    /**
     * 여행지 위치 정보 (공통 Location enum 사용)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location", length = 50)
    private Location location;

    private LocalDate startTime;
    private LocalDate endTime;
    private Integer recruitLimit;
    private Long viewCount = 0L;
    private Boolean isAddRecruit = false;


    @Builder
    public TravelPost(User user, ChatRoom chatRoom, String title, String content,
                      LocalDate startTime, LocalDate endTime, String imageUrl,
                      Integer recruitLimit, PostType postType, Boolean isAddRecruit, Location location) {
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
        this.location = location;
    }


    //=========비즈니스 로직==========

    // 게시글 수정 (DTO 기반)
    public void update(PostType postType, TravelPostRequest request, String imageUrl) {
        if (postType == PostType.BEFORE) {
            validateUpdateData(request.getTitle(), request.getContent(), request.getStartTime(),
                    request.getEndTime(), request.getRecruitLimit());
        } else if (postType == PostType.NOW) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다.");
            }
        } else throw new BusinessException(ErrorCode.UNSUPPORTED_POST_TYPE);

        this.title = request.getTitle();
        this.content = request.getContent();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        this.imageUrl = imageUrl;
        this.recruitLimit = request.getRecruitLimit();
        this.isAddRecruit = request.getIsAddRecruit();
        this.location = request.getLocationAsEnum();
    }

    // 내부 검증 로직
    private void validateUpdateData(String title, String content, LocalDate startTime,
                                    LocalDate endTime, Integer recruitLimit) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("시작 날짜는 필수입니다.");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("종료 날짜는 필수입니다.");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
        if (recruitLimit == null || recruitLimit < 1) {
            throw new IllegalArgumentException("모집 인원은 1명 이상이어야 합니다.");
        }
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 작성자 본인 확인
    public boolean isAuthor(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 게시글 삭제 가능 여부 확인
    public boolean canBeDeleted() {
        // 참가자가 없는 경우에만 삭제 가능
        return this.viewCount == 0; // 간단한 예시, 실제로는 참가자 수 확인 필요
    }

    // 모집 상태 확인 (모집 중인지 완료인지)
    public boolean isRecruiting() {
        return this.isAddRecruit;
    }

    // 모집 상태 업데이트
    public void updateRecruitStatus(boolean isRecruiting) {
        this.isAddRecruit = isRecruiting;
    }

    // 모집 완료 여부 확인 (참가자 수 기반)
    public boolean isRecruitmentFull(int approvedParticipantCount) {
        return approvedParticipantCount >= this.recruitLimit;
    }

    // 모집 상태 변경
    public void updateRecruitmentStatus(Boolean isAddRecruit) {
        this.isAddRecruit = isAddRecruit;
    }
} 