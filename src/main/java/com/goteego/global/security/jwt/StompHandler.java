package com.goteego.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            log.info("✅ Stomp Handshake 요청 수신");

            // 토큰 추출
            String token = extractToken(accessor);
            log.info("token = {}", token);

            // 토큰 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {
//                String email = jwtTokenProvider.getEmailFromToken(token);
                Long userId = jwtTokenProvider.getUserId(token);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                accessor.setUser(auth); // SecurityContext에 정보 저장
                log.info("✅ WebSocket 인증 성공. 사용자 ID: {}", userId);
            } else {
                log.warn("❌ WebSocket 인증 실패");
                throw new AccessDeniedException("JWT 토큰이 유효하지 않습니다.");
            }
        }
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // 헤더에서 Authorization 추출 [확인]
        List<String> headers = accessor.getNativeHeader("Authorization");
        if (headers != null && !headers.isEmpty()) {
            return headers.get(0).replace("Bearer ", "");
        }

        // 쿠키에서 accessToken 추출
        List<String> cookies = accessor.getNativeHeader("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                log.info("cookie = {}", cookie);
                for (String part : cookie.split(";")) {
                    String[] keyValue = part.trim().split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("accessToken")) {
                        return keyValue[1];
                    }
                }
            }
        }
        return null;
    }
}