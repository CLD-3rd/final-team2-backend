package com.goteego.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goteego.chat.service.redis.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // 캐시 활성화
public class RedisConfig {

    @Value("${spring.data.redis.password}")
    String password;
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    // Redis 커넥션 객체 생성
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Object Mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        // 객체 값을 Json 형식으로 변환해주는 시리얼라이저
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // Redis Message Listener (이벤트 리스너)
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter,
                                                              ChannelTopic chatTopic, ChannelTopic notificationTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // [chat] 채널로부터 메시지가 오면 listenerAdapter가 처리하도록 설정
        container.addMessageListener(listenerAdapter, chatTopic);
        // [notification] 채널로부터 메시지가 오면 listenerAdapter가 처리하도록 설정
        container.addMessageListener(listenerAdapter, notificationTopic);
        return container;
    }

    // 실제 메시지를 처리할 Subscriber(RedisSubscriber)와 처리할 메서드(sendMessage)를 연결해주는 Adapter
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        // RedisSubscriber 클래스의 sendMessage 메서드를 리스너의 기본 메서드로 사용
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }


    // Pub/Sub에서 사용할 채팅 관련 공용 채널 정의
    @Bean
    @Qualifier("chatTopic")
    public ChannelTopic chatTopic() {
        // 여기서는 모든 채팅 메시지를 "chat"이라는 단일 토픽으로 처리
        return new ChannelTopic("chat");
    }

    // Pub/Sub에서 사용할 알림 관련 공용 채널 정의
    @Bean
    @Qualifier("notificationTopic")
    public ChannelTopic notificationTopic() {
        return new ChannelTopic("notification");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}