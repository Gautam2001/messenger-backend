package com.Messenger.Utility;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.Messenger.Utility.Security.CustomUserDetailsService;
import com.Messenger.Utility.Security.JwtHandshakeInterceptor;
import com.Messenger.Utility.Security.JwtUtil;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customUserDetailsService;

	public WebSocketConfig(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
		super();
		this.jwtUtil = jwtUtil;
		this.customUserDetailsService = customUserDetailsService;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		CommonUtils.logMethodEntry(this);
		stompEndpointRegistry.addEndpoint("/ws") // websocket handshake endpoint
				.setAllowedOriginPatterns("*").addInterceptors(jwtHandshakeInterceptor());
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
		CommonUtils.logMethodEntry(this);
		messageBrokerRegistry.enableSimpleBroker("/topic"); // enables a simple in-memory broker
		messageBrokerRegistry.setApplicationDestinationPrefixes("/messenger");
	}

	@Bean
	HandshakeInterceptor jwtHandshakeInterceptor() {
		return new JwtHandshakeInterceptor(jwtUtil, customUserDetailsService);
	}

}
