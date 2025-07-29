package com.goteego.user.controller;

import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.dto.UserResponse;
import com.goteego.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * ✅ 로그인 사용자 정보 조회
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
        // ✅ RefreshToken DB에서 삭제
        if (user != null) {
            userService.updateRefreshToken(user.getId(), null);
        }
        // ✅ 쿠키 만료
        response.addCookie(CookieUtil.deleteCookie("accessToken"));
        response.addCookie(CookieUtil.deleteCookie("refreshToken"));

        // ✅ SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // ✅ 세션 무효화
        request.getSession().invalidate();

        return ResponseEntity.ok().build();
    }
}
