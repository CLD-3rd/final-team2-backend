package com.goteego.user.domain;

import com.goteego.badge.domain.UserBadge;
import com.goteego.chat.domain.UserChatRoom;
import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // ✅ 이름 변경
    private Long id;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Embedded
    private OauthInfo oauthInfo;

    private boolean isSuspended = false;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Column(name = "review_score_sum", nullable = false)
    private long reviewScoreSum = 0L;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> badges = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();

    @Builder
    public User(String nickname, String profileImgUrl, UserRole role, OauthInfo oauthInfo) {
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.oauthInfo = oauthInfo;
    }

    public static User createDefaultOAuthUser(OauthInfo oauthInfo) {
        return User.builder()
                .nickname(oauthInfo.getName())
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .build();
    }

    public void suspend() {
        this.isSuspended = true;
    }

    public void activate() {
        this.isSuspended = false;
    }

    public void updateProfileImage(String updatedProfileImgUrl) {
        this.profileImgUrl = updatedProfileImgUrl;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public double getAverageRating() {
        return reviewCount == 0 ? 0.0 : Math.round(((double) reviewScoreSum / reviewCount) * 10) / 10.0;
    }

    public void addReview(int rating) {
        this.reviewCount++;
        this.reviewScoreSum += rating;
    }

    public void removeReview(int rating) {
        if (reviewCount > 0) {
            this.reviewCount--;
            this.reviewScoreSum -= rating;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}