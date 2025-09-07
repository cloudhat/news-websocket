package assignment.newswebsocket.repository;

import assignment.newswebsocket.constant.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
class NewsSessionTest {

    @Autowired
    private NewsInMemorySession sut;

    @BeforeEach
    void setUp() {
        //자바 객체이기 때문에 가능한 처리, 실제로는 별도의 초기화 메서드가 필요함
        sut.clearAllSessions();
    }

    @DisplayName("세션 정상 생성")
    @Test
    void createSession() {
        //given
        String clientId = "client123";
        String socketId = "socket123";

        //when
        sut.createSession(clientId, socketId);

        //then
        assertThat(sut.isSessionPresent(clientId)).isTrue();
    }

    @DisplayName("세션 정상 삭제")
    @Test
    void deleteSession() {
        //given
        String clientId = "client123";
        String socketId = "socket123";
        sut.createSession(clientId, socketId);

        //when
        sut.removeSession(socketId);

        //then
        assertThat(sut.isSessionPresent(clientId)).isFalse();
    }

    @DisplayName("세션 갱신")
    @Test
    void refreshSession() {
        //given
        String clientId = "client123";
        String socketId = "socket123";
        sut.createSession(clientId, socketId);

        //when
        sut.refreshSession(socketId);

        //then
        assertThat(sut.isSessionPresent(clientId)).isTrue();
    }

    @DisplayName("동일 토큰으로 다중 연결 시도")
    @Test
    void duplicatedCreateSession() {
        //given
        String clientId = "client123";
        String socketId1 = "socketId1";
        sut.createSession(clientId, socketId1);

        //when & then
        String socketId2 = "socketId2";

        assertThatThrownBy(() -> sut.createSession(clientId, socketId2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(CustomException.SOCKET_ALREADY_CONNECTED.getMessage());
    }

}
