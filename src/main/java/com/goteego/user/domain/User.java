package com.goteego.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Embedded
    private OauthInfo oauthInfo;

    @Builder
    public User(String name, String email, String password, UserRole role, OauthInfo oauthInfo) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.oauthInfo = oauthInfo;
    }

    public static User createDefaultOAuthMember(OauthInfo oauthInfo) {
        return User.builder()
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .build();
    }

    public boolean isMatchingPassword(String password) {
        return this.password.equals(password);
    }
}