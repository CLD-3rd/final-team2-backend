package com.goteego.user.controller;

import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserDto(user.getId(), user.getOauthInfo().getOauthEmail()));
    }
    @GetMapping
    public List<UserDto> searchAllUsers(@AuthenticationPrincipal User user) {
        return userService.findAllExcludingMe(user.getId());
    }
}
