package assignment.newswebsocket.repository;

public interface NewsSession {

    void createSession(String clientId, String socketId);

    void removeSession(String socketId);

    boolean isSessionPresent(String clientId);

    void refreshSession(String socketId);
}
