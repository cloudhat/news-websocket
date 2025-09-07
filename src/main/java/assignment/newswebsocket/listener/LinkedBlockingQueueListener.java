package assignment.newswebsocket.listener;

import assignment.newswebsocket.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@RequiredArgsConstructor
public class LinkedBlockingQueueListener {

    private final BlockingQueue<String> newsQueue = new LinkedBlockingQueue<>();
    private final NewsService newsService;

    public void receiveNewsId(String newsId) {
        newsQueue.add(newsId);
        processQueue();
    }

    /**
     * 큐에 쌓인 메시지를 하나씩 처리
     * 실제 SQSListener 교체 시 이 부분이 @SqsListener 메서드로 대체되야 함
     */
    private void processQueue() {
        String newsId;
        while ((newsId = newsQueue.poll()) != null) {
            newsService.processAndBroadcast(newsId);
        }
    }

}
