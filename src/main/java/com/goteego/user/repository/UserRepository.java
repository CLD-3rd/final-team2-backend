package com.goteego.user.repository;

import com.goteego.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthInfo_OauthEmail(String oauthEmail);

    @Query("SELECT u.refreshToken FROM User u WHERE u.id = :userId")
    Optional<String> findRefreshTokenByUserId(@Param("userId") Long userId);

    Optional<User> findByOauthInfoOauthId(String oauthId);

    Optional<User> findByOauthInfo_OauthIdAndOauthInfo_OauthProvider(String oauthId, String oauthProvider);


}
