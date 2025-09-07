package assignment.newswebsocket.service;

import assignment.newswebsocket.repository.NewsSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static assignment.newswebsocket.config.WebSocketConfig.END_POINT;
import static assignment.newswebsocket.service.WebSocketService.SOCKET_TOKEN_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketServiceTest {

    @LocalServerPort
    int port;

    @Autowired
    private NewsSession newsSession;

    WebSocketStompClient webSocketStompClient;


    @BeforeEach
    void setup() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        webSocketStompClient = new WebSocketStompClient(sockJsClient);
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @DisplayName("WebSocket 연결 및 해제 시 NewsSession이 올바르게 업데이트 되는지 테스트")
    @Test
    void connectAndDisconnect_ShouldUpdateNewsSession() throws Exception {
        //given
        String clientId = "testClient";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(SOCKET_TOKEN_HEADER, clientId);

        //when
        StompSession stompSession = webSocketStompClient.connect(
                "ws://localhost:" + port + END_POINT,
                new WebSocketHttpHeaders(),        // HTTP Handshake Header
                connectHeaders,          // STOMP Connect Header
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);

        // polling 방식으로 최대 2초 대기
        await().atMost(Duration.ofSeconds(2))
                .until(() -> newsSession.isSessionPresent(clientId));

        // 세션 끊기
        stompSession.disconnect();

        // NewsSession에서 세션이 제거됐는지 확인
        await().atMost(Duration.ofSeconds(2))
                .until(() -> !newsSession.isSessionPresent(clientId));
    }

    @DisplayName("동일 토큰으로 연결 시도시 요청 차단 테스트")
    @Test
    void duplicatedSession_ShouldThrowException() throws Exception {
        //given
        String clientId = "duplicateClient";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(SOCKET_TOKEN_HEADER, clientId);

        StompSession session1 = webSocketStompClient.connect(
                "ws://localhost:" + port + END_POINT,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(2))
                .until(() -> newsSession.isSessionPresent(clientId));

        //when & then
        Throwable thrown = assertThrows(ExecutionException.class, () -> {
            webSocketStompClient.connect(
                    "ws://localhost:" + port + END_POINT,
                    new WebSocketHttpHeaders(),
                    connectHeaders,
                    new StompSessionHandlerAdapter() {
                    }
            ).get(1, TimeUnit.SECONDS);
        });

        assertThat(thrown.getCause())
                .isInstanceOf(ConnectionLostException.class);

        //finally
        session1.disconnect();
    }

    @DisplayName("Heartbeat 요청 시 세션이 갱신되는지 테스트")
    @Test
    void heartbeat_ShouldRefreshSession() throws Exception {
        //given
        String clientId = "testClient";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(SOCKET_TOKEN_HEADER, clientId);

        StompSession session = webSocketStompClient.connect(
                "ws://localhost:" + port + END_POINT,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);

        //when
        StompHeaders heartbeatHeaders = new StompHeaders();
        heartbeatHeaders.setDestination("/app/heartbeat");
        session.send(heartbeatHeaders, null);

        //then
        await().atMost(Duration.ofSeconds(2))
                .until(() -> newsSession.isSessionPresent(clientId));

        //finally
        session.disconnect();
    }

}