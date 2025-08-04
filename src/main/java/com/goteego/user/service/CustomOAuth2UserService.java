package com.goteego.user.service;

import com.goteego.global.s3.S3Service;
import com.goteego.user.domain.CustomOAuth2User;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // ex) "google"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // ✅ 구글 기준 기본 정보
        String oauthId = (String) attributes.get("sub");
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");
        String pictureUrl = (String) attributes.get("picture");

        log.info("[OAuth2] 로그인 요청: {}, {}, {} {}", name, email, oauthId, pictureUrl);

        // ✅ OauthInfo 객체 생성
        OauthInfo oauthInfo = OauthInfo.builder()
                .oauthId(oauthId)
                .oauthProvider(registrationId)
                .oauthEmail(email)
                .name(name)
                .build();

        // ✅ 기존 사용자 여부 확인
        User user = userRepository.findByOauthInfoOauthId(oauthId)
                .orElseGet(() -> {
                    log.info("[OAuth2] 새로운 사용자 등록: {}", email);
                    return userRepository.save(User.createDefaultOAuthUser(oauthInfo, null));
                });

        // ✅ CustomUserPrincipal 반환
        return new CustomOAuth2User(user, attributes);
    }
}
