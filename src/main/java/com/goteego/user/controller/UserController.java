package com.goteego.user.controller;

import com.goteego.global.security.jwt.RefreshTokenService;
import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.dto.UserProfileResponse;
import com.goteego.user.dto.UserResponse;
import com.goteego.user.dto.UserUpdateRequest;
import com.goteego.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    /**
     * ✅ 사용자 조회
     * 현재 로그인한 사용자의 간단한 정보를 조회합니다.
     *
     * @param user
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // ✅ 401
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * ✅ 사용자 프로필 조회
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     *
     * @param user 인증된 사용자 객체
     * @return 사용자 프로필 응답 (UserProfileResponse)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal User user) {

        UserProfileResponse response = userService.getUserProfile(user.getId());
        log.info("✅ [User] 사용자 프로필 조회 성공 - userNickName: {}", user.getNickname());
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 모든 사용자 검색 (자기 자신 제외)
     *
     * @param user
     * @return
     */
    @GetMapping
    public List<UserDto> searchAllUsers(@AuthenticationPrincipal User user) {
        return userService.findAllExcludingMe(user.getId());
    }

    /**
     * ✅ 로그아웃 처리
     *
     * @param user
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user, HttpServletRequest request, HttpServletResponse response) {
        // ✅ RefreshToken Redis에서 삭제
        if (user != null) {
            refreshTokenService.deleteRefreshToken(user.getId());
        }
        // ✅ 쿠키 삭제 (AccessToken, RefreshToken)
        response.addCookie(CookieUtil.deleteCookie("accessToken"));
        response.addCookie(CookieUtil.deleteCookie("refreshToken"));

        // ✅ SecurityContext 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }

    /**
     * ✅ 사용자 프로필 수정 API<br>
     * - 닉네임 및 프로필 이미지 동시 수정 가능<br>
     * - 프로필 이미지는 MultipartFile 형식으로 전송 (옵션)<br>
     * - 기존 이미지가 있으면 삭제 후 새 이미지 업로드<br>
     * - 이미지가 전달되지 않으면 기존 이미지 유지
     *
     * @param user    인증된 사용자 정보
     * @param request 닉네임 및 프로필 이미지 수정 요청
     * @return 수정된 사용자 프로필 정보
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfileImage(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute UserUpdateRequest request) {

        String oldNickname = user.getNickname();
        UserResponse response = userService.updateUserProfile(user.getId(), request);
        log.info("✅ [User] 프로필 수정 성공 - userNickName: {} -> {}", oldNickname, response.nickname());
        return ResponseEntity.ok(response);
    }
}
