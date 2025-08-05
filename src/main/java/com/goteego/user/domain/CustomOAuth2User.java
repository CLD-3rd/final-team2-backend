package com.goteego.user.domain;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder
public class CustomOAuth2User implements OAuth2User, Serializable {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String email;
    private final String role;
    private final Map<String, Object> attributes;

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public String getName() {
        return email;
    }
}