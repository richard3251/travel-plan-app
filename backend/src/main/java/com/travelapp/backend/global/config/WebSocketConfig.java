package com.travelapp.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * STOMP 프로토콜을 사용한 실시간 채팅 구현
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     * -/topic: 1:N (브로드캐스트)
     * -/queue: 1:1 (개인 메시지)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지를 보낼 때 prefix
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 메시지를 보낼 때 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지 보낼 때 prefix
        config.setUserDestinationPrefix("/user");
    }

    /**
     * WebSocket 엔드포인트 등록
     * 클라이언트가 연결할 URL
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*") // CORS 설정 (개발 환경이라 다 열어둠)
            .withSockJS(); // SockJS fallback 옵션 (WebSocket 미지원 브라우저 대응)
    }

}
