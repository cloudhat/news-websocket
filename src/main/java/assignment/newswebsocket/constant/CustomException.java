package assignment.newswebsocket.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomException {

    SOCKET_ALREADY_CONNECTED(409, "이미 연결된 소켓입니다."),
    REFRESH_NEWS_SESSION_FAILED(500, "세션 갱신에 실패했습니다."),;

    private final int status;
    private final String message;

}
