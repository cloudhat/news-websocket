package assignment.newswebsocket.config;

import assignment.newswebsocket.repository.NewsSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final NewsSession newsSession;
    public static final String END_POINT = "/ws/news";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String APP_PREFIX = "/app";
    public static final String SOCKET_TOKEN_HEADER = "SOCKET_TOKEN";

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        converters.add(new MappingJackson2MessageConverter());
        return false;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(END_POINT).setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(TOPIC_PREFIX);
        registry.setApplicationDestinationPrefixes(APP_PREFIX);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    connect(accessor);
                }
                else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    disconnect(accessor);
                }
                return message;
            }
        });
    }

    //일반적으로 JWT토큰을 헤더에 담아 보내지만, 여기서는 간단히 클라이언트 아이디를 담아 보낸다.
    private void connect(StompHeaderAccessor accessor) {
        String clientId = accessor.getFirstNativeHeader(SOCKET_TOKEN_HEADER);
        String sessionId = accessor.getSessionId();
        newsSession.createSession(clientId, sessionId);
    }

    private void disconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        newsSession.removeSession(sessionId);
    }
}
