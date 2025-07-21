package com.goteego.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Embedded
    private OauthInfo oauthInfo;

    @Builder
    public User(UserRole role, OauthInfo oauthInfo) {
        this.role = role;
        this.oauthInfo = oauthInfo;
    }

    public static User createDefaultOAuthMember(OauthInfo oauthInfo) {
        return User.builder()
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .build();
    }
}