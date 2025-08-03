package com.goteego.global.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker  // STOMP 메시지 브로커 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final HttpHandshakeInterceptor httpHandshakeInterceptor;

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
                .addInterceptors(httpHandshakeInterceptor) // 핸드셰이크 인터셉터 추가
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