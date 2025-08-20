package com.goteego.k6;

import com.goteego.global.security.jwt.JwtTokenProvider;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@Profile("!prod")
@RequiredArgsConstructor
public class TestSetUpController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/setup/users")
    @Transactional
    public ResponseEntity<List<UserInfo>> seedTestUsers(@RequestBody SeedRequest request) {
//        userRepository.deleteAllInBatch(); // 기존 사용자 모두 삭제

        List<User> usersToSave = new ArrayList<>();
        for (int i = 1; i <= request.getCount(); i++) {
            OauthInfo oauthInfo = OauthInfo.builder()
                    .oauthId("test-oauth-id-" + i)
                    .oauthProvider("google")
                    .oauthEmail("test-user-" + i + "@example.com")
                    .name("Test User " + i)
                    .build();
            usersToSave.add(User.createDefaultOAuthUser(oauthInfo));
        }

        List<User> savedUsers = userRepository.saveAll(usersToSave);

        // ✅ 생성된 사용자들의 실제 ID와 이메일을 DTO 리스트로 만들어 반환
        List<UserInfo> response = savedUsers.stream()
                .map(u -> new UserInfo(u.getId(), u.getOauthInfo().getOauthEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/token")
    public ResponseEntity<String> issueTestToken(@RequestBody AuthRequest request) {
        User user = userRepository.findByOauthInfoOauthEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Test user not found: " + request.getEmail()));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        return ResponseEntity.ok(accessToken);
    }


    //== DTO ==//

    public static class UserInfo {
        private Long id;
        private String email;

        // Lombok @Data 또는 수동으로 Getter/Setter/Constructor 생성
        public UserInfo(Long id, String email) { this.id = id; this.email = email; }
        public Long getId() { return id; }
        public String getEmail() { return email; }
    }

    public static class SeedRequest {
        private int count;
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class AuthRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}