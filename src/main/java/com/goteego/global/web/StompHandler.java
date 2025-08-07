package com.goteego.global.web;

import com.goteego.global.security.jwt.JwtTokenProvider;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * StompHandler 흐름
 * ㄴ JWTFilter에서 WHITE LIST에 /ws가 포함되어 있으므로 JWT 검증을 건너뜀
 * ㄴ /ws로 WebSocket 연결을 시도하면 HTTP 핸드셰이크가 발생 -> STOMP 프로토콜 단계에서 StompHandler가 개입
 * ㄴ CONNECT 명령이 오면 preSend() 메서드가 호출
 * ㄴ 헤더나 쿠키에서 JWT 토큰을 추출하여 직접 검증
 * ㄴ 검증 성공 시, accessor.setUser()로 인증 정보를 설정
 *
 * 바뀐구조
 * ㄴ js에서는 WebSocket 핸드셰이크를 위해 "/api/ws-ticket"의 경로로 API 호출
 * ㄴ ticket(UUID)를 STOMP 헤더에 Authorization: ticket 구조로 서버에 전달
 * ㄴ StompHandler는 ticket을 추출하여 redis에서 userId를 조회
 * ㄴ userService의 getUser(userId)를 통해 실제 User 객체 반환
 * ㄴ UsernamePasswordAuthenticationToken(user) 수행
 * ㄴ accessor에 auth 저장
 * ㄴ ChatController에서 Principal 사용
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;

    // 클라이언트에서 STOMP 헤더에 'Authorization'으로 티켓을 담아 보내기로 약속 (티켓의 내용은 단순히 UUID)
    private static final String TICKET_HEADER = "Authorization";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 요청일 때만 인증 처리
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("✅ STOMP CONNECT 요청 처리 시작");

            // 1. 헤더에서 인증 티켓 추출
            String ticket = accessor.getFirstNativeHeader(TICKET_HEADER);
            if (ticket == null || ticket.isBlank()) {
                log.error("❌ STOMP CONNECT 에러: 인증 티켓이 헤더에 없습니다.");
                throw new AccessDeniedException("인증 티켓이 필요합니다.");
            }

            // 2. Redis에서 티켓으로 사용자 ID 조회
            String redisKey = "ws-ticket:" + ticket;
            String userIdStr = redisTemplate.opsForValue().get(redisKey);

            if (userIdStr == null) {
                log.error("❌ STOMP CONNECT 에러: 유효하지 않거나 만료된 티켓입니다. Ticket: {}", ticket);
                throw new AccessDeniedException("유효하지 않거나 만료된 티켓입니다.");
            }

            // 3. 사용된 티켓은 즉시 삭제 (일회용으로 만듦)
            redisTemplate.delete(redisKey);
            log.info("✅ 티켓 사용 완료 및 삭제. Ticket: {}", ticket);

            // 4. 사용자 정보로 Principal 객체 생성 및 세션에 등록
            Long userId = Long.parseLong(userIdStr);
            User user = userService.getUserById(userId);
            log.warn("userId = {}", user.getId());
            log.warn("user Name = {}", user.getNickname());
            log.warn("user email = {}", user.getOauthInfo().getOauthEmail());


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            accessor.setUser(authentication);

            if (user != null) {
                log.info("✅ WebSocket 인증 성공. 사용자 ID: {}, 세션 사용자: {}", userId, authentication.getName());
            }
        }

        return message;
    }
}