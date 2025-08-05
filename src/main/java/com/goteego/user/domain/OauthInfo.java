package com.goteego.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public String oauthId;
    private String oauthProvider;
    @Column(nullable = false, unique = true)
    private String oauthEmail;
    @Column(name = "oauth_name")
    private String name;

    @Builder
    public OauthInfo(String oauthId, String oauthProvider, String oauthEmail, String name) {
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
        this.oauthEmail = oauthEmail;
        this.name = name;
    }
}