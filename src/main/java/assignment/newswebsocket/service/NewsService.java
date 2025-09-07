package assignment.newswebsocket.service;

import assignment.newswebsocket.repository.NewsSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static assignment.newswebsocket.constant.CustomException.REFRESH_NEWS_SESSION_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsSession newsSession;
    private final SimpMessagingTemplate messagingTemplate;

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
