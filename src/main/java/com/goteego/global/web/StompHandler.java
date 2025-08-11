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
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class StompHandler implements ChannelInterceptor {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final UserService userService;
//
//    // 클라이언트에서 STOMP 헤더에 'Authorization'으로 티켓을 담아 보내기로 약속 (티켓의 내용은 단순히 UUID)
//    private static final String TICKET_HEADER = "Authorization";
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        // STOMP CONNECT 요청일 때만 인증 처리
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            log.info("✅ STOMP CONNECT 요청 처리 시작");
//            String podName = System.getenv("HOSTNAME"); // Pod 이름 가져오기
//
//            // 1. 헤더에서 인증 티켓 추출
//            String ticket = accessor.getFirstNativeHeader(TICKET_HEADER);
//            if (ticket == null || ticket.isBlank()) {
//                log.error("❌ STOMP CONNECT 에러: 인증 티켓이 헤더에 없습니다.");
//                throw new AccessDeniedException("인증 티켓이 필요합니다.");
//            }
//
//            // 2. Redis에서 티켓으로 사용자 ID 조회
//            String redisKey = "ws-ticket:" + ticket;
//            String userIdStr = redisTemplate.opsForValue().get(redisKey);
//
//            if (userIdStr == null) {
//                log.error("❌ STOMP CONNECT 에러: 유효하지 않거나 만료된 티켓입니다. Ticket: {}", ticket);
//                throw new AccessDeniedException("유효하지 않거나 만료된 티켓입니다.");
//            }
//
//            // 3. 사용된 티켓은 즉시 삭제 (일회용으로 만듦)
//            redisTemplate.delete(redisKey);
//            log.info("✅ 티켓 사용 완료 및 삭제. Ticket: {}", ticket);
//
//            // 4. 사용자 정보로 Principal 객체 생성 및 세션에 등록
//            Long userId = Long.parseLong(userIdStr);
//            User user = userService.getUserById(userId);
//            log.warn("userId = {}", user.getId());
//            log.warn("podName = {}", podName);
//            log.warn("user Name = {}", user.getNickname());
//            log.warn("user email = {}", user.getOauthInfo().getOauthEmail());
//
//
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
//            accessor.setUser(authentication);
//
//            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
//            if (sessionAttributes != null) {
//                sessionAttributes.put("userId", userId);
//            }
//
//            if (user != null) {
//                log.info("✅ WebSocket 인증 성공. 사용자 ID: {}, 세션 사용자: {}", userId, authentication.getName());
//            }
//        }
//
//        return message;
//    }
//}


// 21:00
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
            String podName = System.getenv("HOSTNAME"); // Pod 이름 가져오기

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
            log.warn("podName = {}", podName);


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    Collections.emptyList()
            );
            accessor.setUser(authentication);

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                // Spring Security와 WebSocket 메시징이 세션을 식별하는 데 사용하는 표준 키
                sessionAttributes.put("user", authentication);
                // 애플리케이션에서 사용하기 위한 userId
                sessionAttributes.put("userId", userId);
            } else {
                // 로깅 추가: 세션 속성이 null인 비정상적인 경우를 대비
                log.error("!!!!!!!!!! StompHeaderAccessor.getSessionAttributes() is NULL !!!!!!!!!!");
            }

            log.info("✅ WebSocket 인증 성공. Principal Name: {}, Session UserID: {}", authentication.getName(), userId);
        }

        return message;
    }
}