package com.goteego.user.service;

import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * OAuth 로그인 유저 저장 또는 기존 유저 조회
     */
    @Transactional
    public User registerOrGetOAuthUser(OauthInfo oauthInfo) {
        return userRepository.findByOauthInfo_OauthIdAndOauthInfo_OauthProvider(
                        oauthInfo.getOauthId(), oauthInfo.getOauthProvider()
                )
                .orElseGet(() -> {
                    User newUser = User.createDefaultOAuthMember(oauthInfo);
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public void updateRefreshToken(Long userId, String newToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setRefreshToken(newToken); // setter 필요
    }
}