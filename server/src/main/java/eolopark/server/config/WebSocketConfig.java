package eolopark.server.config;

import eolopark.server.service.NotificationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * This component is used to configure websocket connections.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationService handler;

    public WebSocketConfig (NotificationService handler) {
        this.handler = handler;
    }

    public void registerWebSocketHandlers (WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/notifications").addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}