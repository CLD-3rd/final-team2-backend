package com.goteego.websocket.controller;

import com.goteego.global.security.jwt.JwtTokenProvider;
import com.goteego.websocket.dto.TicketResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebSocketTicketController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * WebSocket 연결을 위한 일회용 티켓을 발급하는 API.
     * HttpOnly 쿠키의 accessToken을 검증하여 티켓을 발급하고 Redis에 저장한다.
     */
    @GetMapping("/api/ws-ticket")
    public ResponseEntity<TicketResponse> issueWebSocketTicket(@CookieValue("accessToken") String accessToken) {
        // 1. 쿠키의 JWT 토큰 유효성 검증
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            throw new AccessDeniedException("유효하지 않은 토큰입니다.");
        }

        // 2. 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(accessToken);

        // 3. 일회용 티켓 생성 및 Redis에 저장 (유효시간 30초)
        String ticket = UUID.randomUUID().toString();
        String redisKey = "ws-ticket:" + ticket;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(userId), Duration.ofSeconds(30));

        log.info("✅ 웹소켓 티켓 발급. Ticket: {}, UserID: {}", ticket, userId);

        // 4. 클라이언트에게 티켓 반환
        return ResponseEntity.ok(new TicketResponse(ticket));
    }

}