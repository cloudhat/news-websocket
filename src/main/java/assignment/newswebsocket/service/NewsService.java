package assignment.newswebsocket.service;

import assignment.newswebsocket.dto.TranslatedNewsResponse;
import assignment.newswebsocket.entity.TranslatedNews;
import assignment.newswebsocket.repository.NewsSession;
import assignment.newswebsocket.repository.TranslatedNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static assignment.newswebsocket.constant.CustomException.REFRESH_NEWS_SESSION_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsSession newsSession;
    private final TranslatedNewsRepository translatedNewsRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public static final String NEWS_TOPIC = "/topic/news";

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

    public void processAndBroadcast(String id) {

        Optional<TranslatedNews> optional = translatedNewsRepository.findById(id);

        //필요에 따라 DLQ로 이동시키는 로직 추가 가능
        if (!optional.isPresent()) {
            log.info("Translated News not found for id: " + id);
            return;
        }

        /**
         * At-least-once delivery 보장이 필요하다면 아래 2가지 변경점이 추가되어야 한다
         * 메시지 전송 성공 여부 확인 및 실패 시 재시도 로직 및 DLQ 이동 로직 추가
         * 클라이언트에서 메시지 중복 처리 로직 구현
         */
        try {
            messagingTemplate.convertAndSend(NEWS_TOPIC, new TranslatedNewsResponse(optional.get()));
        } catch (Exception e) {
            log.error("Failed to broadcast news id: " + id, e);
        }
    }

}
