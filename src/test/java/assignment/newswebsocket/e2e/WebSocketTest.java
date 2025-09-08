package assignment.newswebsocket.e2e;

import assignment.newswebsocket.repository.NewsSession;
import assignment.newswebsocket.util.WebSocketUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketTest {

    @LocalServerPort
    int port;

    @SpyBean
    private NewsSession newsSession;


    @DisplayName("WebSocket 연결 및 해제 시 NewsSession이 올바르게 업데이트 되는지 테스트")
    @Test
    void connectAndDisconnect_ShouldUpdateNewsSession() throws Exception {
        //given
        String clientId = "testClient";

        //when
        StompSession stompSession = WebSocketUtil.getSession(port, clientId);

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

        StompSession session1 = WebSocketUtil.getSession(port, clientId);
        await().atMost(Duration.ofSeconds(2))
                .until(() -> newsSession.isSessionPresent(clientId));

        //when & then
        Throwable thrown = assertThrows(ExecutionException.class, () -> {
            WebSocketUtil.getSession(port, clientId);
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

        StompSession session = WebSocketUtil.getSession(port, clientId);

        //when
        StompHeaders heartbeatHeaders = new StompHeaders();
        heartbeatHeaders.setDestination("/app/heartbeat");
        session.send(heartbeatHeaders, null);

        //then
        await().atMost(Duration.ofSeconds(2))
                .until(() -> newsSession.isSessionPresent(clientId));
        verify(newsSession, timeout(2000).times(1)).refreshSession(anyString());

        //finally
        session.disconnect();
    }

}