package assignment.newswebsocket.e2e;

import assignment.newswebsocket.dto.TranslatedNewsResponse;
import assignment.newswebsocket.entity.TranslatedNews;
import assignment.newswebsocket.listener.LinkedBlockingQueueListener;
import assignment.newswebsocket.repository.TranslatedNewsRepository;
import assignment.newswebsocket.util.IntegrationTest;
import assignment.newswebsocket.util.WebSocketUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NewsServiceTest extends IntegrationTest {

    @Autowired
    private LinkedBlockingQueueListener linkedBlockingQueueListener;

    @Autowired
    private TranslatedNewsRepository translatedNewsRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    private String newsId1 = "a1b2c3";

    @LocalServerPort
    int port;

    @BeforeEach
    void setGivenData() {
        TranslatedNews news = new TranslatedNews(
                newsId1,
                "Qraft AI Product팀 개발자 채용",
                "핀테크 스타트업 크래프트 테크놀로지스(QRAFT Technologies)는",
                LocalDateTime.of(2025, 6, 5, 10, 0, 0)
        );
        translatedNewsRepository.save(news);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @DisplayName("뉴스 발행 및 브로드캐스트 테스트")
    @Test
    void testProcessAndBroadcast() throws ExecutionException, InterruptedException, TimeoutException {

        //given
        String clientId = "testClient";

        StompSession session = WebSocketUtil.getSession(port, clientId);

        BlockingQueue<TranslatedNewsResponse> receivedMessages = new LinkedBlockingQueue<>();

        session.subscribe("/topic/news", new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    byte[] byteArray = (byte[]) payload;
                    TranslatedNewsResponse news = objectMapper.readValue(byteArray, TranslatedNewsResponse.class);
                    receivedMessages.add(news);
                } catch (Exception e) {
                    System.out.println("Error processing message: " + e.getMessage());
                }
            }
        });

        //when
        linkedBlockingQueueListener.receiveNewsId(newsId1);

        //then
        TranslatedNewsResponse message1 = receivedMessages.poll(1, TimeUnit.SECONDS);
        assertThat(message1.getId()).isEqualTo(newsId1);

        // finally
        session.disconnect();
    }



}
