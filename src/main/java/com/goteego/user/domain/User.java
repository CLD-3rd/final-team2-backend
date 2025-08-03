package com.goteego.user.domain;

import com.goteego.chat.domain.UserChatRoom;
import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();

    @Builder
    public User(String nickname, String profileImgUrl, UserRole role, OauthInfo oauthInfo) {
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.oauthInfo = oauthInfo;
    }

    public static User createDefaultOAuthUser(OauthInfo oauthInfo, String profileImgUrl) {
        return User.builder()
                .nickname(oauthInfo.getName())
                .profileImgUrl(profileImgUrl)
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .build();
    }

    public void setIsSuspend(boolean isSuspended) {
        this.isSuspended = isSuspended;
        // BaseEntity의 lastModifiedAt이 자동으로 업데이트됨
    }

    public void updateProfileImage(String updatedProfileImgUrl) {
        this.profileImgUrl = updatedProfileImgUrl;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }
}