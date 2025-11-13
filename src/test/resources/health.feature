Feature: Health Check
  Scenario: 시스템이 건강한지 확인한다
    When 클라이언트가 GET /health API를 요청하면
    Then 응답 상태 코드는 200 이어야 한다
    And 응답 본문은 "OK" 이어야 한다

  Scenario: MySQL 연결을 확인한다
    When 애플리케이션이 MySQL에서 SELECT 1 쿼리를 실행하면
    Then 쿼리 결과는 1이어야 한다

  Scenario: Redis 연결을 확인한다
    When 애플리케이션이 Redis에 키를 저장하면
    Then 동일한 값을 반환해야 한다

  Scenario: Kafka 연결을 확인한다
    When 애플리케이션이 Kafka로 메시지를 전송하면
    Then 동일한 메시지를 컨슘해야 한다
