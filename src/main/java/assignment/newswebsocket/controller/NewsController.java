package assignment.newswebsocket.controller;

import assignment.newswebsocket.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        newsService.refreshSession(sessionId);
    }

}