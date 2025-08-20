package com.goteego.k6;

import com.goteego.feed.repository.FeedCommentRepository;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.security.jwt.JwtTokenProvider;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/test")
//@Profile("!prod")
//@RequiredArgsConstructor
//public class TestSetUpController {
//
//    private final UserRepository userRepository;
//    private final JwtTokenProvider jwtTokenProvider;
//
//    @PostMapping("/setup/users")
//    @Transactional
//    public ResponseEntity<List<UserInfo>> seedTestUsers(@RequestBody SeedRequest request) {
//        userRepository.deleteAllInBatch(); // 기존 사용자 모두 삭제
//
//        List<User> usersToSave = new ArrayList<>();
//        for (int i = 1; i <= request.getCount(); i++) {
//            OauthInfo oauthInfo = OauthInfo.builder()
//                    .oauthId("test-oauth-id-" + i)
//                    .oauthProvider("google")
//                    .oauthEmail("test-user-" + i + "@example.com")
//                    .name("Test User " + i)
//                    .build();
//            usersToSave.add(User.createDefaultOAuthUser(oauthInfo));
//        }
//
//        List<User> savedUsers = userRepository.saveAll(usersToSave);
//
//        // ✅ 생성된 사용자들의 실제 ID와 이메일을 DTO 리스트로 만들어 반환
//        List<UserInfo> response = savedUsers.stream()
//                .map(u -> new UserInfo(u.getId(), u.getOauthInfo().getOauthEmail()))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/auth/token")
//    public ResponseEntity<String> issueTestToken(@RequestBody AuthRequest request) {
//        User user = userRepository.findByOauthInfoOauthEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("Test user not found: " + request.getEmail()));
//
//        String accessToken = jwtTokenProvider.createAccessToken(user);
//        return ResponseEntity.ok(accessToken);
//    }
//
//
//    //== DTO ==//
//
//    public static class UserInfo {
//        private Long id;
//        private String email;
//
//        // Lombok @Data 또는 수동으로 Getter/Setter/Constructor 생성
//        public UserInfo(Long id, String email) { this.id = id; this.email = email; }
//        public Long getId() { return id; }
//        public String getEmail() { return email; }
//    }
//
//    public static class SeedRequest {
//        private int count;
//        public int getCount() { return count; }
//        public void setCount(int count) { this.count = count; }
//    }
//
//    public static class AuthRequest {
//        private String email;
//        public String getEmail() { return email; }
//        public void setEmail(String email) { this.email = email; }
//    }
//}

@RestController
@RequestMapping("/test")
@Profile("!prod")
@RequiredArgsConstructor
public class TestSetUpController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    // 💣 [추가] 연관 데이터 삭제를 위한 Repository 주입
    private final FeedRepository feedRepository;
    private final FeedCommentRepository feedCommentRepository;
    // ... 만약 다른 연관 테이블이 있다면 여기에 Repository를 추가 ...

    @PostMapping("/setup/users")
    @Transactional
    public ResponseEntity<List<UserInfo>> seedTestUsers(@RequestBody SeedRequest request) {

        if (request.isCleanup()) {
            // 1. 피드 댓글 삭제
            feedCommentRepository.deleteAllInBatch();
            // 2. 피드 삭제
            feedRepository.deleteAllInBatch();
            // 3. 마지막으로 사용자 삭제
            userRepository.deleteAllInBatch();
        }


        // 💣 [수정] 기존 테스트 사용자 수를 파악하여 시작 인덱스를 설정
        long existingTestUserCount = userRepository.countByOauthInfoOauthEmailStartingWith("test-user-");
        int startIndex = (int) existingTestUserCount + 1;

        List<User> usersToSave = new ArrayList<>();
        for (int i = startIndex; i < startIndex + request.getCount(); i++) {
            OauthInfo oauthInfo = OauthInfo.builder()
                    .oauthId("test-oauth-id-" + i)
                    .oauthProvider("google")
                    .oauthEmail("test-user-" + i + "@example.com")
                    .name("Test User " + i)
                    .build();
            usersToSave.add(User.createDefaultOAuthUser(oauthInfo));
        }

        List<User> savedUsers = userRepository.saveAll(usersToSave);

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

    // UserInfo DTO (기존과 동일)
    @Getter
    public static class UserInfo {
        private Long id;
        private String email;
        public UserInfo(Long id, String email) { this.id = id; this.email = email; }
    }

    // 💣 [수정] SeedRequest DTO에 cleanup 필드 추가
    @Getter
    @Setter
    public static class SeedRequest {
        private int count;
        // 데이터 초기화 여부를 결정하는 플래그 (기본값: true)
        private boolean cleanup = true;
    }

    // AuthRequest DTO (기존과 동일)
    @Getter
    @Setter
    public static class AuthRequest {
        private String email;
    }
}