<h1>실시간 뉴스 알림 API 서버 구축</h1>
<h2>실행 방법</h2>
프로젝트의 루트 디렉토리에서 아래의 명령어로 도커 컴포즈 파일 실행 <br>
docker compose up --build
    
<h2>AWS SQS로 전환 시 변경되어야 할 부분</h2>
    <ol>
        <li>별도의 클래스를 생성하고 SQSListener를 이용해 receiveMessage 메소드를 구현한다</li>
        <li> 구현한 클래스의 receiveMessage 메소드에서 NewsService 클래스의 processAndBroadcast 메소드를 호출하도록 구현한다</li>
        <li>LinkedBlockingQueueListener 클래스를 제거한다</li>
        <li>운영 파이프라인에서 SQS에 뉴스 ID를 적재하도록 변경한다</li>
        <li>뉴스 알림 API 서버를 운영배포한다 </li>
    </ol>

<h2>구현사항</h2>
<ul>
  <li>웹소켓 서버 구성, 큐 처리 및 뉴스전달, 동일 토큰 다중 연결 방지 등의 비즈니스 요구사항 구현이 쉽게 파악 가능하도록 테스트코드를 구현했습니다.</li>  
  <li>동일 토큰으로 다중 연결을 시도할 경우 이후에 시도한 요청을 차단하도록 Caffeine Cache을 이용하여 별도의 세션을 구현했습니다. 단 멀티서버의 경우에는 Redis 등으로 교체해야 합니다.</li>
  <li>DB에서 뉴스 조회 실패시 로깅을 하도록 구현했습니다. 또한 필요할 경우 DLQ로 처리 가능함을 명시했습니다.</li>
  <li>메시지 전송이 실패할 경우 로깅하도록 구현했습니다. 또한 At-least-once delivery 보장이 필요할 경우 필요한 구현도 명시했습니다. </li>
</ul>
<h2>사용 기술</h2>
<ul>
    <li>Spring Boot</li>
    <li>Stomp</li>
    <li>Spring Data JPA</li>
    <li>H2 Database</li>
    <li>Caffeine Cache</li>
    <li>JUnit 5</li>
</ul>
