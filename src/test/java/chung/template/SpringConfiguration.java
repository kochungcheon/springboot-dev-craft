package chung.template;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
public class SpringConfiguration {

    private static final String TESTCONTAINERS_CONFIG_FILE = "/application-test.yml";

    private static final MySQLContainer<?> MYSQL;
    private static final GenericContainer<?> REDIS;
    private static final KafkaContainer KAFKA;
    private static final int REDIS_PORT;

    static {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = SpringConfiguration.class.getResourceAsStream(TESTCONTAINERS_CONFIG_FILE);
            Map<String, Object> yamlProps = yaml.load(inputStream);
            Map<String, Object> testcontainersProps = (Map<String, Object>) yamlProps.get("testcontainers");

            Map<String, Object> mysqlProps = (Map<String, Object>) testcontainersProps.get("mysql");
            Map<String, Object> redisProps = (Map<String, Object>) testcontainersProps.get("redis");
            Map<String, Object> kafkaProps = (Map<String, Object>) testcontainersProps.get("kafka");

            MYSQL = new MySQLContainer<>((String) mysqlProps.get("image"))
                    .withDatabaseName((String) mysqlProps.get("database"))
                    .withUsername((String) mysqlProps.get("username"))
                    .withPassword((String) mysqlProps.get("password"));

            REDIS_PORT = (int) redisProps.get("port");
            REDIS = new GenericContainer<>(DockerImageName.parse((String) redisProps.get("image")))
                    .withExposedPorts(REDIS_PORT);

            KAFKA = new KafkaContainer(DockerImageName.parse((String) kafkaProps.get("image")));

            MYSQL.start();
            REDIS.start();
            KAFKA.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Testcontainers from " + TESTCONTAINERS_CONFIG_FILE, e);
        }
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }
}