package com.goteego.user.domain;

import com.goteego.chat.domain.UserChatRoom;
import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // ✅ 이름 변경
    private Long id;

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Embedded
    private OauthInfo oauthInfo;

    private boolean isSuspended = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();

    @Builder
    public User(String nickname, String profileImgUrl, UserRole role, OauthInfo oauthInfo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.oauthInfo = oauthInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createDefaultOAuthUser(OauthInfo oauthInfo, String profileImgUrl) {
        return User.builder()
                .nickname(oauthInfo.getName())
                .profileImgUrl(profileImgUrl)
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setIsSuspend(boolean isSuspended) {
        this.isSuspended = isSuspended;
        this.updatedAt = LocalDateTime.now();
    }
}