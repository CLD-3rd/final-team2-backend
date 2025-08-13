package com.goteego.global.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//@Configuration
//@EnableWebSocketMessageBroker
//@RequiredArgsConstructor
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final StompHandler stompHandler;
//    private final HttpHandshakeInterceptor httpHandshakeInterceptor;
//
//    @Value("${spring.rabbitmq.host}")
//    private String relayHost;
//
//    @Value("${spring.rabbitmq.username}")
//    private String clientLogin;
//
//    @Value("${spring.rabbitmq.password}")
//    private String clientPasscode;
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        // 클라이언트 -> 서버로 메시지를 보낼 때 (메시지 발행)
//        registry.setApplicationDestinationPrefixes("/pub");
//
//        // 서버 -> 클라이언트로 메시지를 보낼 때 (메시지 구독)
//        registry.enableStompBrokerRelay("/topic", "/queue")
//                .setRelayHost(relayHost)
//                .setRelayPort(61613)
//                .setClientLogin(clientLogin)
//                .setClientPasscode(clientPasscode)
//                // Docker 환경에서는 systemLogin/Passcode가 clientLogin/Passcode와 동일해도 무방
//                .setSystemLogin(clientLogin)
//                .setSystemPasscode(clientPasscode)
//                .setSystemHeartbeatSendInterval(10000)
//                .setSystemHeartbeatReceiveInterval(10000);
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .addInterceptors(httpHandshakeInterceptor)
//                .withSockJS();
//
//        registry.addEndpoint("/ws-raw")
//                .setAllowedOriginPatterns("*")
//                .addInterceptors(httpHandshakeInterceptor);
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(stompHandler);
//        registration.taskExecutor()
//                .corePoolSize(4)
//                .maxPoolSize(8)
//                .queueCapacity(500);
//    }
//}



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.username}")
    private String clientLogin;

    @Value("${spring.rabbitmq.password}")
    private String clientPasscode;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(61613)
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode)
                .setSystemLogin(clientLogin)
                .setSystemPasscode(clientPasscode)
                .setSystemHeartbeatSendInterval(10000)
                .setSystemHeartbeatReceiveInterval(10000);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 모든 인터셉터와 핸들러를 제거한 가장 단순한 엔드포인트만 남깁니다.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}