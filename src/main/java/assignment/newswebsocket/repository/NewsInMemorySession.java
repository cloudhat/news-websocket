package assignment.newswebsocket.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import assignment.newswebsocket.constant.CustomException;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NewsInMemorySession implements NewsSession {

    private final Cache<String, String> clientSessions =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

    private final Cache<String, String> socketIdToClientIdMap =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

    public void createSession(String clientId, String socketId) {
        if (!isSessionPresent(clientId)) {
            socketIdToClientIdMap.put(socketId, clientId);
            clientSessions.put(clientId, socketId);
        } else {
            log.error("Session already exists for clientId: " + clientId);
            throw new IllegalStateException(CustomException.SOCKET_ALREADY_CONNECTED.getMessage());
        }
    }

    public void removeSession(String socketId) {
        String clientId = socketIdToClientIdMap.getIfPresent(socketId);
        if (clientId != null) {
            clientSessions.invalidate(clientId);
        }
        socketIdToClientIdMap.invalidate(socketId);
    }

    public boolean isSessionPresent(String clientId) {
        return clientSessions.getIfPresent(clientId) != null;
    }

    public void refreshSession(String socketId) {
        String clientId = socketIdToClientIdMap.getIfPresent(socketId);
        if (clientId != null) {
            // 접근 시 TTL이 갱신됨
            socketIdToClientIdMap.put(socketId, clientId);
            clientSessions.put(clientId, socketId);
        }
    }

    // 테스트 용도: 모든 세션을 초기화, 레디스 등 다른 저장소 사용 시 제거 필요
    public void clearAllSessions() {
        clientSessions.invalidateAll();
        socketIdToClientIdMap.invalidateAll();
    }

}
