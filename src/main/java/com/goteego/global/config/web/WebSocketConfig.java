package com.goteego.global.config.web;

import com.goteego.global.jwt.JwtTokenProvider;
import com.goteego.global.jwt.StompHandler;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * [1] registry.enableSimpleBroker (서버 -> 클라이언트)
 * ㄴ 브로드캐스트(다수 구독자에게 동일 메시지 전달) 용
 * ㄴ 주로 채팅방, 공용 알림 등 여러 사용자가 동일한 메시지를 수신해야 할 경우 사용
 * ㄴ 예) /topic/chatroom.123 → 방 123에 들어온 사람들은 다 같이 받음
 *
 * [2] registry.setApplicationDestinationPrefixes (클라이언트 -> 서버)
 * 클라이언트가 서버로 메시지를 보낼 때 사용할 접두사(prefix) 혹은 경로 설정
 * ㄴ 실제로는 @MessageMapping이 붙은 컨트롤러 메서드로 라우팅되며, 서버 내부 로직을 처리
 * ㄴ 예) 클라이언트가 /app/chat.sendMessage로 메시지를 보내면, 서버 컨트롤러의 @MessageMapping("/chat.sendMessage") 메서드가 호출됨.
 */
@Configuration
@EnableWebSocketMessageBroker  // STOMP 메시지 브로커 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // SimpleBroker는 해당하는 경로로 구독하는 client에게 메시지를 전달하는 작업 수행
        registry.enableSimpleBroker("/sub", "/queue"); // 구독 경로
        // 클라이언트가 메시지를 보낼 때, 경로 앞에 /pub이 붙어있으면 Broker로 전달
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지 발행 경로
        registry.setUserDestinationPrefix("/user"); // 1:1 채팅 전용 prefix로, 이후 /user/{userId}/queue/messages로 라우팅
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 연결 전에 WebSocket을 먼저 핸드셰이크를 위한 주소 설정 (클라이언트가 WebSocket에 연결할 때 해당 엔드포인트 "/ws"로 접근)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()  // SockJS 폴백 지원 (브라우저 호환성)
                .setSessionCookieNeeded(true) // ✅ 쿠키 전송 허용
                .setSuppressCors(true);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 메시지 인바운드 채널 인터셉터 설정
        registration.interceptors(stompHandler);
    }

}