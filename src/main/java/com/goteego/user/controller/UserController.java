package com.goteego.user.controller;

import com.goteego.user.domain.CustomOAuth2User;
import com.goteego.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @GetMapping("/test")
    public void test(@AuthenticationPrincipal User user) {
        log.info("user id = {}", user.getId());
    }

}
