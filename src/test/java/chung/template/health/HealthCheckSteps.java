package chung.template.health;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckSteps {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private KafkaProperties kafkaProperties;

	private ResponseEntity<String> httpResponse;
	private Integer mysqlQueryResult;
	private String redisStoredValue;
	private String redisRetrievedValue;
	private String kafkaConsumedMessage;
	private String kafkaPayload;

	@When("클라이언트가 GET \\/health API를 요청하면")
	public void a_client_requests_the_health_api() {
		httpResponse = restTemplate.getForEntity("/health", String.class);
	}

	@Then("응답 상태 코드는 200 이어야 한다")
	public void the_response_status_code_should_be_200() {
		assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@And("응답 본문은 {string} 이어야 한다")
	public void the_response_body_should_be(String body) {
		assertThat(httpResponse.getBody()).isEqualTo(body);
	}

	@When("애플리케이션이 MySQL에서 SELECT 1 쿼리를 실행하면")
	public void execute_mysql_query() {
		mysqlQueryResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
	}

	@Then("쿼리 결과는 {int}이어야 한다")
	public void verify_mysql_result(int expected) {
		assertThat(mysqlQueryResult).isEqualTo(expected);
	}

	@When("애플리케이션이 Redis에 키를 저장하면")
	public void store_value_in_redis() {
		redisStoredValue = "alive";
		String key = "health:key";
		stringRedisTemplate.opsForValue().set(key, redisStoredValue);
		redisRetrievedValue = stringRedisTemplate.opsForValue().get(key);
	}

	@Then("동일한 값을 반환해야 한다")
	public void verify_redis_value() {
		assertThat(redisRetrievedValue).isEqualTo(redisStoredValue);
	}

	@When("애플리케이션이 Kafka로 메시지를 전송하면")
	public void send_message_to_kafka() {
		String topic = "health-check";
		kafkaPayload = "payload-" + UUID.randomUUID();
		consumeKafkaMessage(topic);
	}

	@Then("동일한 메시지를 컨슘해야 한다")
	public void verify_kafka_message() {
		assertThat(kafkaConsumedMessage).isEqualTo(kafkaPayload);
	}

	private void consumeKafkaMessage(String topic) {
		Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "health-check-tests-" + UUID.randomUUID());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer())) {
			consumer.subscribe(Collections.singleton(topic));
			kafkaTemplate.send(topic, kafkaPayload).get(5, TimeUnit.SECONDS);
			kafkaTemplate.flush();
			ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, topic);
			kafkaConsumedMessage = record.value();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Kafka send interrupted", e);
		} catch (ExecutionException | TimeoutException e) {
			throw new IllegalStateException("Kafka send failed", e);
		}
	}
}
