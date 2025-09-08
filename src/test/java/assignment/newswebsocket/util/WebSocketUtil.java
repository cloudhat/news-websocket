package assignment.newswebsocket.util;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static assignment.newswebsocket.config.WebSocketConfig.END_POINT;
import static assignment.newswebsocket.config.WebSocketConfig.SOCKET_TOKEN_HEADER;

public class WebSocketUtil {

    public static StompSession getSession(int port, String clientId) throws ExecutionException, InterruptedException, TimeoutException {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(SOCKET_TOKEN_HEADER, clientId);


        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return webSocketStompClient.connect(
                "ws://localhost:" + port + END_POINT,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);
    }
}
