package com.goteego.user.repository;

import com.goteego.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthInfo_OauthEmail(String oauthEmail);

    Optional<User> findByOauthInfoOauthId(String oauthId);

    Optional<User> findByOauthInfo_OauthIdAndOauthInfo_OauthProvider(String oauthId, String oauthProvider);

    Optional<User> findByOauthInfoOauthEmail(String email);

    long countByOauthInfoOauthEmailStartingWith(String prefix);
}
