package com.goteego.user.controller;

import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.dto.UserResponse;
import com.goteego.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                UserResponse.of(
                        user.getId(),
                        user.getNickname(),
                        user.getProfileImgUrl(),
                        user.getOauthInfo().getOauthEmail()
                )
        );
    }

    @GetMapping
    public List<UserDto> searchAllUsers(@AuthenticationPrincipal User user) {
        return userService.findAllExcludingMe(user.getId());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user, HttpServletResponse response) {
        // ✅ RefreshToken DB에서 삭제
        userService.updateRefreshToken(user.getId(), null);

        // ✅ 쿠키 만료
        response.addCookie(CookieUtil.deleteCookie("accessToken"));
        response.addCookie(CookieUtil.deleteCookie("refreshToken"));

        return ResponseEntity.ok().build();
    }
}
