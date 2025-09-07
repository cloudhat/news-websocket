package assignment.newswebsocket.service;

import assignment.newswebsocket.repository.NewsSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static assignment.newswebsocket.constant.CustomException.REFRESH_NEWS_SESSION_FAILED;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final NewsSession newsSession;
    private final SimpMessagingTemplate messagingTemplate;

    public static final String SOCKET_TOKEN_HEADER = "SOCKET_TOKEN";


    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        newsSession.removeSession(sessionId);
    }

    public void refreshSession(String sessionId) {
        try {
            newsSession.refreshSession(sessionId);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/errors",
                    REFRESH_NEWS_SESSION_FAILED.getMessage()
            );
        }

    }
}
